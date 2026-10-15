package com.emergencymed.request.service;

import com.emergencymed.common.contract.InventoryService;
import com.emergencymed.common.contract.NotificationPublisher;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.common.enums.Role;
import com.emergencymed.request.entity.EmergencyRequest;
import com.emergencymed.request.entity.RequestItem;
import com.emergencymed.request.entity.RequestStatusHistory;
import com.emergencymed.request.exception.AccessForbiddenException;
import com.emergencymed.request.exception.InvalidStateTransitionException;
import com.emergencymed.request.exception.ValidationException;
import com.emergencymed.request.repository.EmergencyRequestRepository;
import com.emergencymed.request.repository.RequestStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RequestStateMachine {

    private static final Logger log = LoggerFactory.getLogger(RequestStateMachine.class);

    private final EmergencyRequestRepository requestRepository;
    private final RequestStatusHistoryRepository historyRepository;
    private final InventoryService inventoryService;
    private final NotificationPublisher notificationPublisher;
    private final PharmacyLookup pharmacyLookup;
    private final int pickupHoldMinutes;

    public RequestStateMachine(
            EmergencyRequestRepository requestRepository,
            RequestStatusHistoryRepository historyRepository,
            InventoryService inventoryService,
            NotificationPublisher notificationPublisher,
            PharmacyLookup pharmacyLookup,
            @org.springframework.beans.factory.annotation.Value("${app.request.pickup-hold-minutes:120}") int pickupHoldMinutes) {
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
        this.inventoryService = inventoryService;
        this.notificationPublisher = notificationPublisher;
        this.pharmacyLookup = pharmacyLookup;
        this.pickupHoldMinutes = pickupHoldMinutes;
    }

    /**
     * 1. New request -> PENDING. Stock reserved before or as part of this transition.
     */
    @Transactional
    public EmergencyRequest transitionToPending(EmergencyRequest request, Long userId) {
        log.info("[STATE] Transitioning request {} to PENDING", request.getReferenceCode());
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        EmergencyRequest saved = requestRepository.save(request);

        recordHistory(saved.getId(), "NEW", "PENDING", userId, "Emergency request created");

        // Notify pharmacy
        Long pharmacyUserId = pharmacyLookup.findApproved(saved.getPharmacyId())
                .map(p -> p.getUserId())
                .orElse(3L);
        notificationPublisher.publish(
                pharmacyUserId,
                "REQUEST_CREATED",
                "New " + saved.getPriority() + " Request",
                "New emergency medicine request " + saved.getReferenceCode() + " received from " + saved.getPatientName(),
                saved.getId()
        );

        return saved;
    }

    /**
     * 2. PENDING -> ACCEPTED, followed by READY_FOR_PICKUP or FINDING_DELIVERY.
     */
    @Transactional
    public EmergencyRequest transitionToAccepted(EmergencyRequest request, Long pharmacyUserId) {
        if (!"PENDING".equals(request.getStatus())) {
            throw new InvalidStateTransitionException("Cannot accept request in status " + request.getStatus());
        }

        // Check if any prescription is pending
        boolean hasUnverifiedRx = request.getItems().stream().anyMatch(RequestItem::isRequiresPrescription);
        if (hasUnverifiedRx && !"VERIFIED".equals(request.getPrescriptionStatus())) {
            throw new InvalidStateTransitionException("Prescription must be verified by pharmacy before accepting request");
        }

        String targetStatus = "SELF_PICKUP".equals(request.getFulfilmentType()) ? "READY_FOR_PICKUP" : "FINDING_DELIVERY";
        log.info("[STATE] Accepting request {}: PENDING -> {}", request.getReferenceCode(), targetStatus);

        request.setStatus(targetStatus);
        request.setAcceptedAt(LocalDateTime.now());
        if ("READY_FOR_PICKUP".equals(targetStatus)) {
            request.setExpiresAt(LocalDateTime.now().plusMinutes(pickupHoldMinutes));
        }
        request.setUpdatedAt(LocalDateTime.now());
        EmergencyRequest saved = requestRepository.save(request);

        recordHistory(saved.getId(), "PENDING", targetStatus, pharmacyUserId, "Pharmacy accepted request");

        // Notify requester
        notificationPublisher.publish(
                saved.getRequesterUserId(),
                "REQUEST_ACCEPTED",
                "Request Accepted",
                "Your emergency request " + saved.getReferenceCode() + " has been accepted by the pharmacy.",
                saved.getId()
        );

        return saved;
    }

    /**
     * 3. PENDING -> REJECTED. Releases reserved stock.
     */
    @Transactional
    public EmergencyRequest transitionToRejected(EmergencyRequest request, Long pharmacyUserId, String reason) {
        if (!"PENDING".equals(request.getStatus())) {
            throw new InvalidStateTransitionException("Cannot reject request in status " + request.getStatus());
        }
        if (reason == null || reason.trim().length() < 10) {
            throw new ValidationException("Rejection reason must be at least 10 characters");
        }

        log.info("[STATE] Rejecting request {}: PENDING -> REJECTED with reason: {}", request.getReferenceCode(), reason);

        // Release stock
        for (RequestItem item : request.getItems()) {
            inventoryService.release(item.getInventoryId(), item.getQuantity(), request.getReferenceCode());
        }

        request.setStatus("REJECTED");
        request.setRejectionReason(reason.trim());
        request.setUpdatedAt(LocalDateTime.now());
        EmergencyRequest saved = requestRepository.save(request);

        recordHistory(saved.getId(), "PENDING", "REJECTED", pharmacyUserId, "Pharmacy rejected: " + reason.trim());

        // Notify requester
        notificationPublisher.publish(
                saved.getRequesterUserId(),
                "REQUEST_REJECTED",
                "Request Rejected",
                "Your request " + saved.getReferenceCode() + " was rejected: " + reason.trim(),
                saved.getId()
        );

        return saved;
    }

    /**
     * 4. PENDING -> EXPIRED after 15 min. Releases reserved stock.
     */
    @Transactional
    public EmergencyRequest transitionToExpiredPending(EmergencyRequest request) {
        if (!"PENDING".equals(request.getStatus())) {
            throw new InvalidStateTransitionException("Cannot expire non-pending request");
        }

        log.info("[STATE] Request {} expired after 15 minutes", request.getReferenceCode());

        // Release stock
        for (RequestItem item : request.getItems()) {
            inventoryService.release(item.getInventoryId(), item.getQuantity(), request.getReferenceCode());
        }

        request.setStatus("EXPIRED");
        request.setUpdatedAt(LocalDateTime.now());
        EmergencyRequest saved = requestRepository.save(request);

        recordHistory(saved.getId(), "PENDING", "EXPIRED", null, "Expired after timeout waiting for pharmacy response");

        notificationPublisher.publish(
                saved.getRequesterUserId(),
                "REQUEST_REJECTED",
                "Request Expired",
                "Emergency request " + saved.getReferenceCode() + " expired without pharmacy response.",
                saved.getId()
        );

        return saved;
    }

    /**
     * 5. FINDING_DELIVERY -> DELIVERY_ASSIGNED.
     */
    @Transactional
    public EmergencyRequest transitionToDeliveryAssigned(EmergencyRequest request, Long partnerUserId) {
        if (!"FINDING_DELIVERY".equals(request.getStatus())) {
            throw new InvalidStateTransitionException("Cannot assign delivery when status is " + request.getStatus());
        }

        log.info("[STATE] Request {} assigned to delivery partner {}", request.getReferenceCode(), partnerUserId);
        request.setStatus("DELIVERY_ASSIGNED");
        request.setDeliveryPartnerUserId(partnerUserId);
        request.setUpdatedAt(LocalDateTime.now());
        EmergencyRequest saved = requestRepository.save(request);

        recordHistory(saved.getId(), "FINDING_DELIVERY", "DELIVERY_ASSIGNED", partnerUserId, "Delivery partner accepted offer");

        notificationPublisher.publish(
                saved.getRequesterUserId(),
                "STATUS_UPDATE",
                "Delivery Partner Assigned",
                "A delivery partner has been assigned to your order " + saved.getReferenceCode(),
                saved.getId()
        );

        return saved;
    }

    /**
     * 6. FINDING_DELIVERY -> READY_FOR_PICKUP when requester switches to pickup.
     */
    @Transactional
    public EmergencyRequest transitionToSwitchToPickup(EmergencyRequest request, Long requesterUserId) {
        if (!"FINDING_DELIVERY".equals(request.getStatus())) {
            throw new InvalidStateTransitionException("Can only switch to self-pickup when finding delivery partner");
        }
        if (!request.getRequesterUserId().equals(requesterUserId)) {
            throw new AccessForbiddenException("Only the requester may switch fulfilment to self-pickup");
        }

        log.info("[STATE] Requester switching request {} to self-pickup", request.getReferenceCode());
        request.setFulfilmentType("SELF_PICKUP");
        request.setStatus("READY_FOR_PICKUP");
        request.setExpiresAt(LocalDateTime.now().plusMinutes(pickupHoldMinutes));
        // Remove delivery fee
        request.setGrandTotal(request.getGrandTotal().subtract(request.getDeliveryFee()));
        request.setDeliveryFee(java.math.BigDecimal.ZERO);
        request.setUpdatedAt(LocalDateTime.now());
        EmergencyRequest saved = requestRepository.save(request);

        recordHistory(saved.getId(), "FINDING_DELIVERY", "READY_FOR_PICKUP", requesterUserId, "Requester switched to self-pickup");

        // Notify pharmacy
        Long pharmacyUserId = pharmacyLookup.findApproved(saved.getPharmacyId()).map(p -> p.getUserId()).orElse(3L);
        notificationPublisher.publish(
                pharmacyUserId,
                "STATUS_UPDATE",
                "Switched to Self-Pickup",
                "Requester switched order " + saved.getReferenceCode() + " to self-pickup",
                saved.getId()
        );

        return saved;
    }

    /**
     * 7. DELIVERY_ASSIGNED -> OUT_FOR_DELIVERY when partner picks up order. Commits stock!
     */
    @Transactional
    public EmergencyRequest transitionToOutForDelivery(EmergencyRequest request, Long partnerUserId) {
        if (!"DELIVERY_ASSIGNED".equals(request.getStatus())) {
            throw new InvalidStateTransitionException("Cannot mark out-for-delivery from status " + request.getStatus());
        }
        if (!partnerUserId.equals(request.getDeliveryPartnerUserId())) {
            throw new AccessForbiddenException("Only the assigned delivery partner may pick up this order");
        }

        log.info("[STATE] Request {} picked up: DELIVERY_ASSIGNED -> OUT_FOR_DELIVERY", request.getReferenceCode());

        // Commit stock
        for (RequestItem item : request.getItems()) {
            inventoryService.commit(item.getInventoryId(), item.getQuantity(), request.getReferenceCode());
        }
        request.setStockCommitted(true);

        request.setStatus("OUT_FOR_DELIVERY");
        request.setUpdatedAt(LocalDateTime.now());
        EmergencyRequest saved = requestRepository.save(request);

        recordHistory(saved.getId(), "DELIVERY_ASSIGNED", "OUT_FOR_DELIVERY", partnerUserId, "Partner picked up order from pharmacy");

        notificationPublisher.publish(
                saved.getRequesterUserId(),
                "STATUS_UPDATE",
                "Order Out For Delivery",
                "Your emergency medicine order " + saved.getReferenceCode() + " is out for delivery.",
                saved.getId()
        );

        return saved;
    }

    /**
     * 8. OUT_FOR_DELIVERY -> DELIVERED after successful handover-code verification.
     */
    @Transactional
    public EmergencyRequest transitionToDelivered(EmergencyRequest request, Long partnerUserId) {
        if (!"OUT_FOR_DELIVERY".equals(request.getStatus())) {
            throw new InvalidStateTransitionException("Cannot mark delivered from status " + request.getStatus());
        }
        if (!partnerUserId.equals(request.getDeliveryPartnerUserId())) {
            throw new AccessForbiddenException("Only the assigned delivery partner may complete this delivery");
        }

        log.info("[STATE] Request {} successfully DELIVERED", request.getReferenceCode());
        request.setStatus("DELIVERED");
        request.setCompletedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        EmergencyRequest saved = requestRepository.save(request);

        recordHistory(saved.getId(), "OUT_FOR_DELIVERY", "DELIVERED", partnerUserId, "Delivery completed with verified handover code");

        notificationPublisher.publish(
                saved.getRequesterUserId(),
                "STATUS_UPDATE",
                "Order Delivered",
                "Your emergency medicine order " + saved.getReferenceCode() + " has been successfully delivered.",
                saved.getId()
        );

        Long pharmacyUserId = pharmacyLookup.findApproved(saved.getPharmacyId()).map(p -> p.getUserId()).orElse(3L);
        notificationPublisher.publish(
                pharmacyUserId,
                "STATUS_UPDATE",
                "Order Delivered",
                "Delivery completed for order " + saved.getReferenceCode(),
                saved.getId()
        );

        return saved;
    }

    /**
     * 9. READY_FOR_PICKUP -> PICKED_UP when pharmacy hands over order. Commits stock!
     */
    @Transactional
    public EmergencyRequest transitionToPickedUp(EmergencyRequest request, Long pharmacyUserId) {
        if (!"READY_FOR_PICKUP".equals(request.getStatus())) {
            throw new InvalidStateTransitionException("Cannot mark picked-up from status " + request.getStatus());
        }

        log.info("[STATE] Request {} picked up at pharmacy", request.getReferenceCode());

        // Commit stock
        for (RequestItem item : request.getItems()) {
            inventoryService.commit(item.getInventoryId(), item.getQuantity(), request.getReferenceCode());
        }
        request.setStockCommitted(true);

        request.setStatus("PICKED_UP");
        request.setCompletedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        EmergencyRequest saved = requestRepository.save(request);

        recordHistory(saved.getId(), "READY_FOR_PICKUP", "PICKED_UP", pharmacyUserId, "Pharmacy completed customer self-pickup handover");

        notificationPublisher.publish(
                saved.getRequesterUserId(),
                "STATUS_UPDATE",
                "Order Handed Over",
                "Your emergency order " + saved.getReferenceCode() + " has been handed over at the pharmacy.",
                saved.getId()
        );

        return saved;
    }

    /**
     * 10. READY_FOR_PICKUP -> EXPIRED after 120 minutes. Releases reserved stock.
     */
    @Transactional
    public EmergencyRequest transitionToExpiredPickup(EmergencyRequest request) {
        if (!"READY_FOR_PICKUP".equals(request.getStatus())) {
            throw new InvalidStateTransitionException("Cannot expire non-pickup request");
        }

        log.info("[STATE] Self-pickup request {} expired after 120 minutes", request.getReferenceCode());

        // Release stock
        for (RequestItem item : request.getItems()) {
            inventoryService.release(item.getInventoryId(), item.getQuantity(), request.getReferenceCode());
        }

        request.setStatus("EXPIRED");
        request.setUpdatedAt(LocalDateTime.now());
        EmergencyRequest saved = requestRepository.save(request);

        recordHistory(saved.getId(), "READY_FOR_PICKUP", "EXPIRED", null, "Self-pickup hold period (120 min) expired");

        notificationPublisher.publish(
                saved.getRequesterUserId(),
                "REQUEST_REJECTED",
                "Pickup Hold Expired",
                "Self-pickup hold for request " + saved.getReferenceCode() + " expired.",
                saved.getId()
        );

        return saved;
    }

    /**
     * 11 & 12. Pre-dispatch and dispatched cancellation.
     */
    @Transactional
    public EmergencyRequest transitionToCancelled(EmergencyRequest request, Long actorUserId, Role actorRole, String reason) {
        if (reason == null || reason.trim().length() < 5) {
            throw new ValidationException("Cancellation reason must be at least 5 characters");
        }

        String curStatus = request.getStatus();

        // Terminal states cannot be cancelled
        if ("DELIVERED".equals(curStatus) || "PICKED_UP".equals(curStatus) || "REJECTED".equals(curStatus) || "EXPIRED".equals(curStatus) || "CANCELLED".equals(curStatus)) {
            throw new InvalidStateTransitionException("Cannot cancel request in terminal status: " + curStatus);
        }

        log.info("[STATE] Cancelling request {}. From status: {}, actor: {} ({})", request.getReferenceCode(), curStatus, actorUserId, actorRole);

        if ("OUT_FOR_DELIVERY".equals(curStatus)) {
            // Rule 12: OUT_FOR_DELIVERY -> CANCELLED only by pharmacy or admin
            if (actorRole != Role.PHARMACY && actorRole != Role.ADMIN) {
                throw new InvalidStateTransitionException("Requester cannot cancel order once it is out for delivery. Only pharmacy or admin can cancel.");
            }
            // Restore committed stock
            if (request.isStockCommitted()) {
                for (RequestItem item : request.getItems()) {
                    inventoryService.restore(item.getInventoryId(), item.getQuantity(), request.getReferenceCode());
                }
            }
        } else {
            // Pre-dispatch states: PENDING, ACCEPTED, FINDING_DELIVERY, DELIVERY_ASSIGNED, READY_FOR_PICKUP
            // Allowed by Requester, Pharmacy, or Admin
            boolean isRequester = actorUserId.equals(request.getRequesterUserId());
            boolean isAdmin = actorRole == Role.ADMIN;
            boolean isPharmacy = actorRole == Role.PHARMACY;

            if (!isRequester && !isAdmin && !isPharmacy) {
                throw new AccessForbiddenException("Not authorized to cancel this request");
            }

            // Release reserved stock
            for (RequestItem item : request.getItems()) {
                inventoryService.release(item.getInventoryId(), item.getQuantity(), request.getReferenceCode());
            }
        }

        request.setStatus("CANCELLED");
        request.setCancelReason(reason.trim());
        request.setCancelledByRole(actorRole.name());
        request.setUpdatedAt(LocalDateTime.now());
        EmergencyRequest saved = requestRepository.save(request);

        recordHistory(saved.getId(), curStatus, "CANCELLED", actorUserId, "Cancelled by " + actorRole + ": " + reason.trim());

        // Notify other parties
        notificationPublisher.publish(
                saved.getRequesterUserId(),
                "STATUS_UPDATE",
                "Request Cancelled",
                "Emergency request " + saved.getReferenceCode() + " was cancelled: " + reason.trim(),
                saved.getId()
        );

        Long pharmacyUserId = pharmacyLookup.findApproved(saved.getPharmacyId()).map(p -> p.getUserId()).orElse(3L);
        if (!actorUserId.equals(pharmacyUserId)) {
            notificationPublisher.publish(
                    pharmacyUserId,
                    "STATUS_UPDATE",
                    "Request Cancelled",
                    "Request " + saved.getReferenceCode() + " was cancelled: " + reason.trim(),
                    saved.getId()
            );
        }

        if (saved.getDeliveryPartnerUserId() != null && !actorUserId.equals(saved.getDeliveryPartnerUserId())) {
            notificationPublisher.publish(
                    saved.getDeliveryPartnerUserId(),
                    "STATUS_UPDATE",
                    "Assignment Cancelled",
                    "Delivery order " + saved.getReferenceCode() + " was cancelled.",
                    saved.getId()
            );
        }

        return saved;
    }

    private void recordHistory(Long requestId, String fromStatus, String toStatus, Long changedBy, String note) {
        RequestStatusHistory history = new RequestStatusHistory(
                requestId,
                fromStatus,
                toStatus,
                changedBy,
                note,
                LocalDateTime.now()
        );
        historyRepository.save(history);
    }
}
