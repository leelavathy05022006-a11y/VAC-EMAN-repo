package com.emergencymed.request.service;

import com.emergencymed.common.enums.Role;

import com.emergencymed.common.contract.*;
import com.emergencymed.common.dto.*;
import com.emergencymed.request.dto.*;
import com.emergencymed.request.entity.EmergencyRequest;
import com.emergencymed.request.entity.Prescription;
import com.emergencymed.request.entity.RequestItem;
import com.emergencymed.request.entity.RequestStatusHistory;
import com.emergencymed.request.exception.*;
import com.emergencymed.request.repository.DeliveryAvailabilityRepository;
import com.emergencymed.request.repository.EmergencyRequestRepository;
import com.emergencymed.request.repository.PrescriptionRepository;
import com.emergencymed.request.repository.RequestStatusHistoryRepository;
import com.emergencymed.request.util.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestService.class);

    private final EmergencyRequestRepository requestRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final RequestStatusHistoryRepository historyRepository;
    private final RequestStateMachine requestStateMachine;
    private final ReferenceCodeGenerator referenceCodeGenerator;
    private final InventoryService inventoryService;
    private final CurrentUserProvider currentUserProvider;
    private final PharmacyLookup pharmacyLookup;
    private final PharmacySettingsLookup settingsLookup;
    private final MedicineLookup medicineLookup;
    private final UserLookup userLookup;
    private final DeliveryAssignmentService deliveryAssignmentService;
    private final DeliveryAvailabilityRepository availabilityRepository;

    private final int pendingExpiryMinutes;
    private final SecureRandom random = new SecureRandom();

    public RequestService(
            EmergencyRequestRepository requestRepository,
            PrescriptionRepository prescriptionRepository,
            RequestStatusHistoryRepository historyRepository,
            RequestStateMachine requestStateMachine,
            ReferenceCodeGenerator referenceCodeGenerator,
            InventoryService inventoryService,
            CurrentUserProvider currentUserProvider,
            PharmacyLookup pharmacyLookup,
            PharmacySettingsLookup settingsLookup,
            MedicineLookup medicineLookup,
            UserLookup userLookup,
            DeliveryAssignmentService deliveryAssignmentService,
            DeliveryAvailabilityRepository availabilityRepository,
            @Value("${app.request.pending-expiry-minutes:15}") int pendingExpiryMinutes) {
        this.requestRepository = requestRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.historyRepository = historyRepository;
        this.requestStateMachine = requestStateMachine;
        this.referenceCodeGenerator = referenceCodeGenerator;
        this.inventoryService = inventoryService;
        this.currentUserProvider = currentUserProvider;
        this.pharmacyLookup = pharmacyLookup;
        this.settingsLookup = settingsLookup;
        this.medicineLookup = medicineLookup;
        this.userLookup = userLookup;
        this.deliveryAssignmentService = deliveryAssignmentService;
        this.availabilityRepository = availabilityRepository;
        this.pendingExpiryMinutes = pendingExpiryMinutes;
    }

    @Transactional
    public RequestDetailResponse createRequest(CreateRequestDto dto) {
        Long requesterUserId = currentUserProvider.userId();
        Role requesterRole = currentUserProvider.role();

        if (requesterRole != Role.PATIENT && requesterRole != Role.HOSPITAL && requesterRole != Role.ADMIN) {
            throw new AccessForbiddenException("Only PATIENT or HOSPITAL can create emergency requests");
        }

        // Check duplicate within 2 minutes
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
        Optional<EmergencyRequest> recentOpt = requestRepository
                .findTopByRequesterUserIdAndPharmacyIdAndCreatedAtAfterOrderByCreatedAtDesc(
                        requesterUserId, dto.getPharmacyId(), twoMinutesAgo);

        if (recentOpt.isPresent()) {
            EmergencyRequest recent = recentOpt.get();
            // Compare items
            if (isSameItems(recent, dto.getItems())) {
                throw new DuplicateResourceException("Duplicate emergency request detected. A request with identical items was placed less than 2 minutes ago.");
            }
        }

        // Validate Pharmacy
        PharmacyDto pharmacy = pharmacyLookup.findApproved(dto.getPharmacyId())
                .orElseThrow(() -> new ValidationException("Pharmacy not found or not approved for emergency requests"));

        // Validate Fulfilment
        BigDecimal deliveryFee = BigDecimal.ZERO;
        if ("DELIVERY".equalsIgnoreCase(dto.getFulfilmentType())) {
            PharmacySettingsDto settings = settingsLookup.find(dto.getPharmacyId())
                    .orElse(new PharmacySettingsDto(dto.getPharmacyId(), true, BigDecimal.valueOf(15.0), BigDecimal.valueOf(50.0)));

            if (!settings.isDeliveryEnabled()) {
                throw new NoDeliveryPartnerException("Delivery is currently disabled for " + pharmacy.getName() + ". Please choose self-pickup.");
            }

            if (dto.getDeliveryAddress() == null || dto.getDeliveryAddress().trim().length() < 10) {
                throw new ValidationException("Delivery address must be at least 10 characters");
            }

            if (dto.getDeliveryLat() != null && dto.getDeliveryLng() != null) {
                double dist = GeoUtils.distanceKm(pharmacy.getLat(), pharmacy.getLng(), dto.getDeliveryLat(), dto.getDeliveryLng());
                if (dist > settings.getDeliveryRadiusKm().doubleValue()) {
                    throw new NoDeliveryPartnerException(String.format(
                            "Delivery address (%.1f km) exceeds the pharmacy's maximum delivery radius of %.1f km. Please choose self-pickup.",
                            dist, settings.getDeliveryRadiusKm().doubleValue()));
                }
            }
            deliveryFee = settings.getDeliveryFee() != null ? settings.getDeliveryFee() : BigDecimal.valueOf(40.00);
        }

        // Validate Distinct Items
        Set<Long> medicineIds = new HashSet<>();
        for (RequestItemDto it : dto.getItems()) {
            if (!medicineIds.add(it.getMedicineId())) {
                throw new ValidationException("Duplicate medicine items are not allowed in the same request");
            }
        }

        // Validate Prescription requirement
        boolean requiresRx = false;
        for (RequestItemDto it : dto.getItems()) {
            MedicineDto med = medicineLookup.findById(it.getMedicineId())
                    .orElseThrow(() -> new ValidationException("Medicine not found with ID: " + it.getMedicineId()));
            if (med.isRequiresPrescription()) {
                requiresRx = true;
            }
        }

        Prescription attachedPrescription = null;
        if (requiresRx) {
            if (dto.getPrescriptionId() == null) {
                throw new PrescriptionRequiredException("A prescription is mandatory for prescription-only medicines in this request");
            }
            attachedPrescription = prescriptionRepository.findById(dto.getPrescriptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + dto.getPrescriptionId()));
        } else if (dto.getPrescriptionId() != null) {
            attachedPrescription = prescriptionRepository.findById(dto.getPrescriptionId()).orElse(null);
        }

        String referenceCode = referenceCodeGenerator.nextReferenceCode();
        String handoverCode = String.format("%04d", random.nextInt(10000));

        // Create Entity
        EmergencyRequest request = new EmergencyRequest();
        request.setReferenceCode(referenceCode);
        request.setRequesterUserId(requesterUserId);
        request.setRequesterType(requesterRole.name());
        request.setPharmacyId(dto.getPharmacyId());
        request.setFulfilmentType(dto.getFulfilmentType().toUpperCase());
        request.setPriority(dto.getPriority().toUpperCase());
        request.setPatientName(dto.getPatientName().trim());
        request.setPatientPhone(dto.getPatientPhone().trim());
        request.setDoctorName(dto.getDoctorName());
        request.setDoctorRegNo(dto.getDoctorRegNo());
        request.setDeliveryAddress(dto.getDeliveryAddress());
        request.setDeliveryLat(dto.getDeliveryLat());
        request.setDeliveryLng(dto.getDeliveryLng());
        request.setHandoverCode(handoverCode);
        request.setDeliveryFee(deliveryFee);
        request.setPaymentMode("DELIVERY".equalsIgnoreCase(dto.getFulfilmentType()) ? "CASH_ON_DELIVERY" : "PAY_AT_PHARMACY");
        request.setPrescriptionStatus(attachedPrescription != null ? attachedPrescription.getVerificationStatus() : "NONE");
        request.setNotes(dto.getNotes());
        request.setExpiresAt(LocalDateTime.now().plusMinutes(pendingExpiryMinutes));

        BigDecimal itemsTotal = BigDecimal.ZERO;

        // Reserve stock and create request items
        for (RequestItemDto it : dto.getItems()) {
            MedicineDto med = medicineLookup.findById(it.getMedicineId()).get();
            BigDecimal price = med.getUnitPrice() != null ? med.getUnitPrice() : BigDecimal.valueOf(50.00);
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(it.getQuantity()));
            itemsTotal = itemsTotal.add(lineTotal);

            // Reserve stock from InventoryService
            Long inventoryId = inventoryService.reserve(dto.getPharmacyId(), it.getMedicineId(), it.getQuantity(), referenceCode);

            RequestItem item = new RequestItem();
            item.setMedicineId(med.getId());
            item.setInventoryId(inventoryId);
            item.setMedicineName(med.getName());
            item.setQuantity(it.getQuantity());
            item.setUnitPrice(price);
            item.setLineTotal(lineTotal);
            item.setRequiresPrescription(med.isRequiresPrescription());
            request.addItem(item);
        }

        request.setItemsTotal(itemsTotal);
        request.setGrandTotal(itemsTotal.add(deliveryFee));

        // Transition to PENDING via state machine
        EmergencyRequest saved = requestStateMachine.transitionToPending(request, requesterUserId);

        if (attachedPrescription != null) {
            attachedPrescription.setRequestId(saved.getId());
            prescriptionRepository.save(attachedPrescription);
        }

        return mapToDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public RequestDetailResponse getRequestDetails(Long id) {
        EmergencyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + id));

        Long currentUserId = currentUserProvider.userId();
        Role role = currentUserProvider.role();

        boolean isRequester = request.getRequesterUserId().equals(currentUserId);
        boolean isAdmin = role == Role.ADMIN;
        boolean isAssignedPartner = currentUserId.equals(request.getDeliveryPartnerUserId());
        Long userPharmId = currentUserProvider.pharmacyId().orElse(null);
        boolean isPharmacy = userPharmId != null && userPharmId.equals(request.getPharmacyId());

        if (!isRequester && !isAdmin && !isAssignedPartner && !isPharmacy) {
            throw new ResourceNotFoundException("Request not found with ID: " + id);
        }

        return mapToDetailResponse(request);
    }

    @Transactional(readOnly = true)
    public LiveTrackingResponse getLiveTracking(Long id) {
        EmergencyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + id));

        Long currentUserId = currentUserProvider.userId();
        Role role = currentUserProvider.role();

        boolean isRequester = request.getRequesterUserId().equals(currentUserId);
        boolean isAdmin = role == Role.ADMIN;
        boolean isAssignedPartner = currentUserId.equals(request.getDeliveryPartnerUserId());
        Long userPharmId = currentUserProvider.pharmacyId().orElse(null);
        boolean isPharmacy = userPharmId != null && userPharmId.equals(request.getPharmacyId());

        if (!isRequester && !isAdmin && !isAssignedPartner && !isPharmacy) {
            throw new AccessForbiddenException("You do not have permission to view live tracking for this request");
        }

        LiveTrackingResponse resp = new LiveTrackingResponse();
        resp.setRequestId(request.getId());
        resp.setReferenceCode(request.getReferenceCode());
        resp.setStatus(request.getStatus());
        resp.setFulfilmentType(request.getFulfilmentType());

        // Pharmacy details
        resp.setPharmacyId(request.getPharmacyId());
        pharmacyLookup.findApproved(request.getPharmacyId()).ifPresent(pharm -> {
            resp.setPharmacyName(pharm.getName());
            resp.setPharmacyAddress(pharm.getAddress());
            resp.setPharmacyPhone(pharm.getPhone());
            resp.setPharmacyLat(pharm.getLat());
            resp.setPharmacyLng(pharm.getLng());
        });

        // Destination details
        resp.setDeliveryAddress(request.getDeliveryAddress());
        resp.setDeliveryLat(request.getDeliveryLat());
        resp.setDeliveryLng(request.getDeliveryLng());

        boolean isDelivery = "DELIVERY".equalsIgnoreCase(request.getFulfilmentType());
        String status = request.getStatus();
        boolean isActiveStatus = "DELIVERY_ASSIGNED".equals(status) || "OUT_FOR_DELIVERY".equals(status);

        resp.setTrackingActive(isDelivery && isActiveStatus);

        if (request.getDeliveryPartnerUserId() != null) {
            resp.setDeliveryPartnerUserId(request.getDeliveryPartnerUserId());
            userLookup.findById(request.getDeliveryPartnerUserId()).ifPresent(u -> {
                resp.setDeliveryPartnerName(u.getFullName());
                resp.setDeliveryPartnerPhone(u.getPhone());
            });

            availabilityRepository.findByPartnerUserId(request.getDeliveryPartnerUserId()).ifPresent(da -> {
                resp.setPartnerLat(da.getCurrentLat());
                resp.setPartnerLng(da.getCurrentLng());
                resp.setLastLocationAt(da.getLastLocationAt());
            });
        }

        // Distance & ETA calculation
        if (isDelivery && resp.getPartnerLat() != null && resp.getPartnerLng() != null) {
            if ("DELIVERY_ASSIGNED".equals(status)) {
                double toPharm = (resp.getPharmacyLat() != null && resp.getPharmacyLng() != null)
                        ? GeoUtils.distanceKm(resp.getPartnerLat(), resp.getPartnerLng(), resp.getPharmacyLat(), resp.getPharmacyLng())
                        : 0.0;
                double pharmToCust = (resp.getPharmacyLat() != null && resp.getPharmacyLng() != null && resp.getDeliveryLat() != null && resp.getDeliveryLng() != null)
                        ? GeoUtils.distanceKm(resp.getPharmacyLat(), resp.getPharmacyLng(), resp.getDeliveryLat(), resp.getDeliveryLng())
                        : 0.0;
                double totalDist = Math.round((toPharm + pharmToCust) * 100.0) / 100.0;
                resp.setRemainingDistanceKm(totalDist);
                resp.setEstimatedArrivalMinutes(Math.max(2, (int) Math.ceil((totalDist / 25.0) * 60)));
                resp.setStatusMessage(resp.getDeliveryPartnerName() != null
                        ? resp.getDeliveryPartnerName() + " is heading to pharmacy for package pickup"
                        : "Courier is heading to pharmacy for package pickup");
            } else if ("OUT_FOR_DELIVERY".equals(status)) {
                if (resp.getDeliveryLat() != null && resp.getDeliveryLng() != null) {
                    double dist = Math.round(GeoUtils.distanceKm(resp.getPartnerLat(), resp.getPartnerLng(), resp.getDeliveryLat(), resp.getDeliveryLng()) * 100.0) / 100.0;
                    resp.setRemainingDistanceKm(dist);
                    resp.setEstimatedArrivalMinutes(Math.max(1, (int) Math.ceil((dist / 25.0) * 60)));
                }
                resp.setStatusMessage(resp.getDeliveryPartnerName() != null
                        ? resp.getDeliveryPartnerName() + " is on the way to your delivery address"
                        : "Courier is on the way to your delivery address");
            } else if ("DELIVERED".equals(status)) {
                resp.setRemainingDistanceKm(0.0);
                resp.setEstimatedArrivalMinutes(0);
                resp.setStatusMessage("Order successfully delivered! Handover verified.");
            } else {
                resp.setStatusMessage("Order status: " + status);
            }
        } else if ("DELIVERED".equals(status)) {
            resp.setRemainingDistanceKm(0.0);
            resp.setEstimatedArrivalMinutes(0);
            resp.setStatusMessage("Order successfully delivered! Handover verified.");
        } else if (!isDelivery) {
            resp.setStatusMessage("Self-pickup order: Collect directly from pharmacy counter.");
        } else if (request.getDeliveryPartnerUserId() == null) {
            resp.setStatusMessage("Finding nearby emergency courier...");
        } else {
            resp.setStatusMessage("Awaiting delivery partner GPS broadcast...");
        }

        return resp;
    }

    @Transactional(readOnly = true)
    public Page<RequestDetailResponse> getMyRequests(String tab, Pageable pageable) {
        Long requesterUserId = currentUserProvider.userId();
        Collection<String> statuses;

        if ("completed".equalsIgnoreCase(tab)) {
            statuses = List.of("DELIVERED", "PICKED_UP");
        } else if ("cancelled".equalsIgnoreCase(tab)) {
            statuses = List.of("CANCELLED", "REJECTED", "EXPIRED");
        } else {
            // active
            statuses = List.of("PENDING", "ACCEPTED", "FINDING_DELIVERY", "DELIVERY_ASSIGNED", "READY_FOR_PICKUP", "OUT_FOR_DELIVERY");
        }

        Page<EmergencyRequest> page = requestRepository.findByRequesterUserIdAndStatusIn(requesterUserId, statuses, pageable);
        return page.map(this::mapToDetailResponse);
    }

    @Transactional(readOnly = true)
    public Page<RequestDetailResponse> getPharmacyRequests(String tab, Pageable pageable) {
        Long pharmacyId = currentUserProvider.pharmacyId()
                .orElseThrow(() -> new AccessForbiddenException("Current user is not associated with an approved pharmacy"));

        Collection<String> statuses;
        if ("in-progress".equalsIgnoreCase(tab) || "inprogress".equalsIgnoreCase(tab)) {
            statuses = List.of("ACCEPTED", "FINDING_DELIVERY", "DELIVERY_ASSIGNED", "READY_FOR_PICKUP", "OUT_FOR_DELIVERY");
        } else if ("completed".equalsIgnoreCase(tab)) {
            statuses = List.of("DELIVERED", "PICKED_UP", "CANCELLED", "REJECTED", "EXPIRED");
        } else {
            // new
            statuses = List.of("PENDING");
        }

        Page<EmergencyRequest> page = requestRepository.findByPharmacyIdAndStatusIn(pharmacyId, statuses, pageable);
        return page.map(this::mapToDetailResponse);
    }

    @Transactional
    public RequestDetailResponse acceptRequest(Long id) {
        EmergencyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));

        Long currentPharmId = currentUserProvider.pharmacyId().orElse(null);
        if (currentPharmId == null || !currentPharmId.equals(request.getPharmacyId())) {
            throw new AccessForbiddenException("Only the assigned pharmacy can accept this request");
        }

        EmergencyRequest updated = requestStateMachine.transitionToAccepted(request, currentUserProvider.userId());

        // If delivery request, start finding delivery partners
        if ("DELIVERY".equalsIgnoreCase(updated.getFulfilmentType())) {
            deliveryAssignmentService.triggerNextDeliveryOffer(updated);
        }

        return mapToDetailResponse(updated);
    }

    @Transactional
    public RequestDetailResponse rejectRequest(Long id, String reason) {
        EmergencyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));

        Long currentPharmId = currentUserProvider.pharmacyId().orElse(null);
        if (currentPharmId == null || !currentPharmId.equals(request.getPharmacyId())) {
            throw new AccessForbiddenException("Only the assigned pharmacy can reject this request");
        }

        EmergencyRequest updated = requestStateMachine.transitionToRejected(request, currentUserProvider.userId(), reason);
        return mapToDetailResponse(updated);
    }

    @Transactional
    public RequestDetailResponse cancelRequest(Long id, String reason) {
        EmergencyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));

        EmergencyRequest updated = requestStateMachine.transitionToCancelled(
                request,
                currentUserProvider.userId(),
                currentUserProvider.role(),
                reason
        );
        return mapToDetailResponse(updated);
    }

    @Transactional
    public RequestDetailResponse switchToPickup(Long id) {
        EmergencyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));

        EmergencyRequest updated = requestStateMachine.transitionToSwitchToPickup(request, currentUserProvider.userId());
        return mapToDetailResponse(updated);
    }

    @Transactional
    public RequestDetailResponse retryDelivery(Long id) {
        EmergencyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));

        Long currentPharmId = currentUserProvider.pharmacyId().orElse(null);
        if (currentPharmId == null || !currentPharmId.equals(request.getPharmacyId())) {
            throw new AccessForbiddenException("Only the pharmacy can retry delivery assignment");
        }

        if (!"FINDING_DELIVERY".equals(request.getStatus())) {
            throw new InvalidStateTransitionException("Can only retry delivery when request is finding delivery");
        }

        // Reset attempts count to allow 3 more attempts
        request.setDeliveryAttempts(0);
        requestRepository.save(request);

        deliveryAssignmentService.triggerNextDeliveryOffer(request);
        return mapToDetailResponse(request);
    }

    @Transactional
    public RequestDetailResponse handoverOrder(Long id) {
        EmergencyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));

        Long currentPharmId = currentUserProvider.pharmacyId().orElse(null);
        if (currentPharmId == null || !currentPharmId.equals(request.getPharmacyId())) {
            throw new AccessForbiddenException("Only the assigned pharmacy can complete handover");
        }

        EmergencyRequest updated = requestStateMachine.transitionToPickedUp(request, currentUserProvider.userId());
        return mapToDetailResponse(updated);
    }

    private boolean isSameItems(EmergencyRequest req, List<RequestItemDto> newItems) {
        if (req.getItems().size() != newItems.size()) return false;
        Map<Long, Integer> m1 = new HashMap<>();
        for (RequestItem i : req.getItems()) {
            m1.put(i.getMedicineId(), i.getQuantity());
        }
        for (RequestItemDto i : newItems) {
            if (!Objects.equals(m1.get(i.getMedicineId()), i.getQuantity())) {
                return false;
            }
        }
        return true;
    }

    private RequestDetailResponse mapToDetailResponse(EmergencyRequest r) {
        RequestDetailResponse resp = new RequestDetailResponse();
        resp.setId(r.getId());
        resp.setReferenceCode(r.getReferenceCode());
        resp.setRequesterUserId(r.getRequesterUserId());
        resp.setRequesterType(r.getRequesterType());
        resp.setPharmacyId(r.getPharmacyId());

        PharmacyDto pharm = pharmacyLookup.findApproved(r.getPharmacyId()).orElse(null);
        if (pharm != null) {
            resp.setPharmacyName(pharm.getName());
            resp.setPharmacyAddress(pharm.getAddress());
            resp.setPharmacyPhone(pharm.getPhone());
            resp.setPharmacyLat(pharm.getLat());
            resp.setPharmacyLng(pharm.getLng());
        }

        resp.setFulfilmentType(r.getFulfilmentType());
        resp.setPriority(r.getPriority());
        resp.setStatus(r.getStatus());
        resp.setPatientName(r.getPatientName());
        resp.setPatientPhone(r.getPatientPhone());
        resp.setDoctorName(r.getDoctorName());
        resp.setDoctorRegNo(r.getDoctorRegNo());
        resp.setDeliveryAddress(r.getDeliveryAddress());
        resp.setDeliveryLat(r.getDeliveryLat());
        resp.setDeliveryLng(r.getDeliveryLng());

        if (r.getDeliveryPartnerUserId() != null) {
            resp.setDeliveryPartnerUserId(r.getDeliveryPartnerUserId());
            userLookup.findById(r.getDeliveryPartnerUserId()).ifPresent(u -> {
                resp.setDeliveryPartnerName(u.getFullName());
                resp.setDeliveryPartnerPhone(u.getPhone());
            });
            availabilityRepository.findByPartnerUserId(r.getDeliveryPartnerUserId()).ifPresent(da -> {
                resp.setDeliveryPartnerLat(da.getCurrentLat());
                resp.setDeliveryPartnerLng(da.getCurrentLng());
                resp.setDeliveryPartnerLastLocationAt(da.getLastLocationAt());
            });
        }

        resp.setItemsTotal(r.getItemsTotal());
        resp.setDeliveryFee(r.getDeliveryFee());
        resp.setGrandTotal(r.getGrandTotal());
        resp.setPaymentMode(r.getPaymentMode());
        resp.setPrescriptionStatus(r.getPrescriptionStatus());

        prescriptionRepository.findByRequestId(r.getId()).ifPresent(p -> {
            resp.setPrescriptionId(p.getId());
            resp.setPrescriptionOriginalName(p.getOriginalName());
        });

        // Handover code visibility: show to requester, and show on verification/completion
        Long curUser = currentUserProvider.userId();
        Role role = currentUserProvider.role();
        if (curUser.equals(r.getRequesterUserId()) || role == Role.ADMIN) {
            resp.setHandoverCode(r.getHandoverCode());
        }

        resp.setDeliveryAttempts(r.getDeliveryAttempts());
        resp.setStockCommitted(r.isStockCommitted());
        resp.setNotes(r.getNotes());
        resp.setRejectionReason(r.getRejectionReason());
        resp.setCancelReason(r.getCancelReason());
        resp.setCancelledByRole(r.getCancelledByRole());
        resp.setExpiresAt(r.getExpiresAt());
        resp.setAcceptedAt(r.getAcceptedAt());
        resp.setCompletedAt(r.getCompletedAt());
        resp.setCreatedAt(r.getCreatedAt());
        resp.setUpdatedAt(r.getUpdatedAt());

        // Items
        List<RequestDetailResponse.ItemDetail> itemDetails = new ArrayList<>();
        for (RequestItem it : r.getItems()) {
            itemDetails.add(new RequestDetailResponse.ItemDetail(
                    it.getId(),
                    it.getMedicineId(),
                    it.getInventoryId(),
                    it.getMedicineName(),
                    it.getQuantity(),
                    it.getUnitPrice(),
                    it.getLineTotal(),
                    it.isRequiresPrescription(),
                    null
            ));
        }
        resp.setItems(itemDetails);

        // History
        List<RequestStatusHistory> historyRows = historyRepository.findByRequestIdOrderByCreatedAtAsc(r.getId());
        List<TimelineItem> timeline = new ArrayList<>();
        for (RequestStatusHistory h : historyRows) {
            String userName = "System";
            if (h.getChangedByUserId() != null) {
                userName = userLookup.findById(h.getChangedByUserId())
                        .map(UserDto::getFullName)
                        .orElse("User #" + h.getChangedByUserId());
            }
            timeline.add(new TimelineItem(
                    h.getFromStatus(),
                    h.getToStatus(),
                    h.getChangedByUserId(),
                    userName,
                    h.getNote(),
                    h.getCreatedAt()
            ));
        }
        resp.setHistory(timeline);

        // Action flags
        String s = r.getStatus();
        boolean isTerminal = "DELIVERED".equals(s) || "PICKED_UP".equals(s) || "REJECTED".equals(s) || "EXPIRED".equals(s) || "CANCELLED".equals(s);
        resp.setCanCancel(!isTerminal && (!"OUT_FOR_DELIVERY".equals(s) || role == Role.PHARMACY || role == Role.ADMIN));
        resp.setCanSwitchToPickup("FINDING_DELIVERY".equals(s) && curUser.equals(r.getRequesterUserId()));
        resp.setCanAccept("PENDING".equals(s) && (role == Role.PHARMACY || role == Role.ADMIN));
        resp.setCanReject("PENDING".equals(s) && (role == Role.PHARMACY || role == Role.ADMIN));
        resp.setCanRetryDelivery("FINDING_DELIVERY".equals(s) && (role == Role.PHARMACY || role == Role.ADMIN));
        resp.setCanHandover("READY_FOR_PICKUP".equals(s) && (role == Role.PHARMACY || role == Role.ADMIN));

        return resp;
    }
}
