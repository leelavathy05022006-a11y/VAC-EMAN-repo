package com.emergencymed.request.service;

import com.emergencymed.common.dto.PharmacyDto;

import com.emergencymed.common.contract.NotificationPublisher;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.request.dto.OfferResponse;
import com.emergencymed.request.entity.DeliveryAssignment;
import com.emergencymed.request.entity.DeliveryAvailability;
import com.emergencymed.request.entity.EmergencyRequest;
import com.emergencymed.request.exception.AccessForbiddenException;
import com.emergencymed.request.exception.InvalidStateTransitionException;
import com.emergencymed.request.exception.ResourceNotFoundException;
import com.emergencymed.request.exception.ValidationException;
import com.emergencymed.request.repository.DeliveryAssignmentRepository;
import com.emergencymed.request.repository.DeliveryAvailabilityRepository;
import com.emergencymed.request.repository.EmergencyRequestRepository;
import com.emergencymed.request.util.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DeliveryAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryAssignmentService.class);

    private final DeliveryAssignmentRepository assignmentRepository;
    private final DeliveryAvailabilityRepository availabilityRepository;
    private final EmergencyRequestRepository requestRepository;
    private final RequestStateMachine requestStateMachine;
    private final PharmacyLookup pharmacyLookup;
    private final NotificationPublisher notificationPublisher;

    private final int offerExpirySeconds;
    private final int maxAttempts;
    private final double searchRadiusKm;

    // Track failed handover attempts per request ID
    private final Map<Long, AtomicInteger> failedHandoverAttempts = new ConcurrentHashMap<>();

    public DeliveryAssignmentService(
            DeliveryAssignmentRepository assignmentRepository,
            DeliveryAvailabilityRepository availabilityRepository,
            EmergencyRequestRepository requestRepository,
            RequestStateMachine requestStateMachine,
            PharmacyLookup pharmacyLookup,
            NotificationPublisher notificationPublisher,
            @Value("${app.delivery.offer-expiry-seconds:120}") int offerExpirySeconds,
            @Value("${app.delivery.max-attempts:3}") int maxAttempts,
            @Value("${app.delivery.search-radius-km:15.0}") double searchRadiusKm) {
        this.assignmentRepository = assignmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.requestRepository = requestRepository;
        this.requestStateMachine = requestStateMachine;
        this.pharmacyLookup = pharmacyLookup;
        this.notificationPublisher = notificationPublisher;
        this.offerExpirySeconds = offerExpirySeconds;
        this.maxAttempts = maxAttempts;
        this.searchRadiusKm = searchRadiusKm;
    }

    @Transactional
    public Optional<DeliveryAssignment> triggerNextDeliveryOffer(EmergencyRequest request) {
        if (!"FINDING_DELIVERY".equals(request.getStatus())) {
            log.info("Request {} is not in FINDING_DELIVERY (status={}), skipping delivery offer", request.getReferenceCode(), request.getStatus());
            return Optional.empty();
        }

        List<DeliveryAssignment> existing = assignmentRepository.findByRequestIdOrderByAttemptNoAsc(request.getId());
        int attemptCount = existing.size();

        if (attemptCount >= maxAttempts) {
            log.warn("Delivery assignment exceeded max attempts ({}) for request {}", maxAttempts, request.getReferenceCode());
            request.setDeliveryAttempts(maxAttempts);
            requestRepository.save(request);

            // Notify requester and pharmacy
            notificationPublisher.publish(
                    request.getRequesterUserId(),
                    "STATUS_UPDATE",
                    "No Delivery Partner Found",
                    "We could not find an available delivery partner after 3 attempts. You can switch to self-pickup now.",
                    request.getId()
            );

            Long pharmacyUserId = pharmacyLookup.findApproved(request.getPharmacyId()).map(p -> p.getUserId()).orElse(3L);
            notificationPublisher.publish(
                    pharmacyUserId,
                    "STATUS_UPDATE",
                    "Delivery Assignment Failed",
                    "No delivery partner accepted request " + request.getReferenceCode() + ". Requester offered self-pickup.",
                    request.getId()
            );

            return Optional.empty();
        }

        Set<Long> alreadyOfferedPartners = new HashSet<>();
        for (DeliveryAssignment a : existing) {
            alreadyOfferedPartners.add(a.getPartnerUserId());
        }

        PharmacyDto pharmacy = pharmacyLookup.findApproved(request.getPharmacyId()).orElse(null);
        BigDecimal pharmLat = pharmacy != null ? pharmacy.getLat() : null;
        BigDecimal pharmLng = pharmacy != null ? pharmacy.getLng() : null;

        List<DeliveryAvailability> availablePartners = availabilityRepository.findByIsAvailableTrue();

        // Filter and sort candidates
        List<Candidate> candidates = new ArrayList<>();
        for (DeliveryAvailability da : availablePartners) {
            Long partnerId = da.getPartnerUserId();
            if (alreadyOfferedPartners.contains(partnerId)) {
                continue;
            }
            if (requestRepository.findActiveJobForPartner(partnerId).isPresent()) {
                continue;
            }

            double dist = GeoUtils.distanceKm(pharmLat, pharmLng, da.getCurrentLat(), da.getCurrentLng());
            if (dist <= searchRadiusKm || da.getCurrentLat() == null) {
                candidates.add(new Candidate(partnerId, dist));
            }
        }

        // Rank nearest first, no location ranked last
        candidates.sort(Comparator.comparingDouble(c -> c.distance));

        if (candidates.isEmpty()) {
            log.warn("No eligible partner found for attempt {} on request {}", attemptCount + 1, request.getReferenceCode());
            if (attemptCount + 1 >= maxAttempts) {
                request.setDeliveryAttempts(maxAttempts);
                requestRepository.save(request);

                notificationPublisher.publish(
                        request.getRequesterUserId(),
                        "STATUS_UPDATE",
                        "No Delivery Partner Available",
                        "No delivery partner could be found. You may switch to self-pickup or wait.",
                        request.getId()
                );
            }
            return Optional.empty();
        }

        Candidate chosen = candidates.get(0);
        int nextAttemptNo = attemptCount + 1;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(offerExpirySeconds);

        DeliveryAssignment assignment = new DeliveryAssignment(
                request.getId(),
                chosen.partnerId,
                "OFFERED",
                nextAttemptNo,
                now,
                expiresAt
        );

        DeliveryAssignment saved = assignmentRepository.save(assignment);
        request.setDeliveryAttempts(nextAttemptNo);
        requestRepository.save(request);

        // Notify chosen partner
        notificationPublisher.publish(
                chosen.partnerId,
                "DELIVERY_OFFER",
                "New Emergency Delivery Offer",
                "Delivery offer #" + nextAttemptNo + " for order " + request.getReferenceCode() + ". Expires in 2 minutes.",
                request.getId()
        );

        return Optional.of(saved);
    }

    private static class Candidate {
        final Long partnerId;
        final double distance;

        Candidate(Long partnerId, double distance) {
            this.partnerId = partnerId;
            this.distance = distance;
        }
    }

    @Transactional(readOnly = true)
    public List<OfferResponse> getOpenOffersForPartner(Long partnerUserId) {
        List<DeliveryAssignment> assignments = assignmentRepository.findByPartnerUserIdAndStatus(partnerUserId, "OFFERED");
        List<OfferResponse> responses = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (DeliveryAssignment da : assignments) {
            if (da.getExpiresAt().isAfter(now)) {
                EmergencyRequest req = requestRepository.findById(da.getRequestId()).orElse(null);
                if (req != null && "FINDING_DELIVERY".equals(req.getStatus())) {
                    PharmacyDto pharm = pharmacyLookup.findApproved(req.getPharmacyId()).orElse(null);
                    OfferResponse r = new OfferResponse();
                    r.setAssignmentId(da.getId());
                    r.setRequestId(req.getId());
                    r.setReferenceCode(req.getReferenceCode());
                    r.setPriority(req.getPriority());
                    r.setPharmacyName(pharm != null ? pharm.getName() : "Approved Pharmacy");
                    r.setPharmacyAddress(pharm != null ? pharm.getAddress() : "");
                    r.setPharmacyLat(pharm != null ? pharm.getLat() : null);
                    r.setPharmacyLng(pharm != null ? pharm.getLng() : null);
                    r.setDeliveryAddress(req.getDeliveryAddress());
                    r.setDeliveryLat(req.getDeliveryLat());
                    r.setDeliveryLng(req.getDeliveryLng());
                    r.setDeliveryFee(req.getDeliveryFee());
                    r.setOfferedAt(da.getOfferedAt());
                    r.setExpiresAt(da.getExpiresAt());
                    r.setSecondsRemaining(Math.max(0, Duration.between(now, da.getExpiresAt()).getSeconds()));
                    responses.add(r);
                }
            }
        }

        return responses;
    }

    @Transactional
    public void acceptOffer(Long assignmentId, Long partnerUserId) {
        DeliveryAssignment assignment = assignmentRepository.findByIdAndPartnerUserId(assignmentId, partnerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + assignmentId));

        if (!"OFFERED".equals(assignment.getStatus())) {
            throw new InvalidStateTransitionException("Offer is no longer available (status=" + assignment.getStatus() + ")");
        }

        if (assignment.getExpiresAt().isBefore(LocalDateTime.now())) {
            assignment.setStatus("EXPIRED");
            assignmentRepository.save(assignment);
            throw new InvalidStateTransitionException("Offer has expired");
        }

        EmergencyRequest request = requestRepository.findById(assignment.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!"FINDING_DELIVERY".equals(request.getStatus())) {
            assignment.setStatus("EXPIRED");
            assignmentRepository.save(assignment);
            throw new InvalidStateTransitionException("Request is no longer finding delivery (status=" + request.getStatus() + ")");
        }

        assignment.setStatus("ACCEPTED");
        assignment.setRespondedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        requestStateMachine.transitionToDeliveryAssigned(request, partnerUserId);
    }

    @Transactional
    public void rejectOffer(Long assignmentId, Long partnerUserId, String reason) {
        DeliveryAssignment assignment = assignmentRepository.findByIdAndPartnerUserId(assignmentId, partnerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + assignmentId));

        if (!"OFFERED".equals(assignment.getStatus())) {
            return;
        }

        assignment.setStatus("REJECTED");
        assignment.setRejectReason(reason);
        assignment.setRespondedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        EmergencyRequest request = requestRepository.findById(assignment.getRequestId()).orElse(null);
        if (request != null && "FINDING_DELIVERY".equals(request.getStatus())) {
            triggerNextDeliveryOffer(request);
        }
    }

    @Transactional
    public void expireOffer(DeliveryAssignment assignment) {
        assignment.setStatus("EXPIRED");
        assignment.setRespondedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        EmergencyRequest request = requestRepository.findById(assignment.getRequestId()).orElse(null);
        if (request != null && "FINDING_DELIVERY".equals(request.getStatus())) {
            triggerNextDeliveryOffer(request);
        }
    }

    @Transactional
    public void markPickedUp(Long requestId, Long partnerUserId) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));
        requestStateMachine.transitionToOutForDelivery(request, partnerUserId);
    }

    @Transactional
    public void markDelivered(Long requestId, Long partnerUserId, String handoverCode) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));

        if (!partnerUserId.equals(request.getDeliveryPartnerUserId())) {
            throw new AccessForbiddenException("You are not the assigned delivery partner for this order");
        }

        if (handoverCode == null || !handoverCode.matches("^[0-9]{4}$")) {
            throw new ValidationException("Handover code must be exactly 4 digits");
        }

        AtomicInteger failures = failedHandoverAttempts.computeIfAbsent(requestId, k -> new AtomicInteger(0));

        if (failures.get() >= 3) {
            throw new AccessForbiddenException("Maximum handover code verification attempts exceeded. Please contact the pharmacy.");
        }

        if (!handoverCode.equals(request.getHandoverCode())) {
            int currentFailures = failures.incrementAndGet();
            if (currentFailures >= 3) {
                // Notify pharmacy of security issue
                Long pharmacyUserId = pharmacyLookup.findApproved(request.getPharmacyId()).map(p -> p.getUserId()).orElse(3L);
                notificationPublisher.publish(
                        pharmacyUserId,
                        "STATUS_UPDATE",
                        "Security Alert: Handover Code Failed 3 Times",
                        "Delivery partner entered incorrect handover code 3 times for order " + request.getReferenceCode(),
                        request.getId()
                );
                throw new AccessForbiddenException("Maximum handover code attempts (3) exceeded. Pharmacy has been notified.");
            } else {
                throw new AccessForbiddenException("Incorrect handover code. Attempt " + currentFailures + " of 3.");
            }
        }

        // Correct code!
        failedHandoverAttempts.remove(requestId);
        requestStateMachine.transitionToDelivered(request, partnerUserId);
    }

    @Transactional(readOnly = true)
    public Page<EmergencyRequest> getPartnerJobs(Long partnerUserId, String type, Pageable pageable) {
        if ("active".equalsIgnoreCase(type)) {
            return requestRepository.findByDeliveryPartnerAndStatusIn(
                    partnerUserId, List.of("DELIVERY_ASSIGNED", "OUT_FOR_DELIVERY"), pageable);
        } else {
            return requestRepository.findByDeliveryPartnerAndStatusIn(
                    partnerUserId, List.of("DELIVERED", "CANCELLED"), pageable);
        }
    }
}
