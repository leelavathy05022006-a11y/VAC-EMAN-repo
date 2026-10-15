package com.emergencymed.request.job;

import com.emergencymed.request.entity.EmergencyRequest;
import com.emergencymed.request.repository.EmergencyRequestRepository;
import com.emergencymed.request.service.RequestStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RequestExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(RequestExpiryJob.class);

    private final EmergencyRequestRepository requestRepository;
    private final RequestStateMachine requestStateMachine;
    private final int pendingExpiryMinutes;
    private final int pickupHoldMinutes;

    public RequestExpiryJob(
            EmergencyRequestRepository requestRepository,
            RequestStateMachine requestStateMachine,
            @Value("${app.request.pending-expiry-minutes:15}") int pendingExpiryMinutes,
            @Value("${app.request.pickup-hold-minutes:120}") int pickupHoldMinutes) {
        this.requestRepository = requestRepository;
        this.requestStateMachine = requestStateMachine;
        this.pendingExpiryMinutes = pendingExpiryMinutes;
        this.pickupHoldMinutes = pickupHoldMinutes;
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void run() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Expire PENDING requests
        LocalDateTime pendingThreshold = now.minusMinutes(pendingExpiryMinutes);
        List<EmergencyRequest> expiredPending = requestRepository.findByStatusAndExpiresAtBefore("PENDING", now);
        for (EmergencyRequest r : expiredPending) {
            try {
                log.info("Expiring pending request: {}", r.getReferenceCode());
                requestStateMachine.transitionToExpiredPending(r);
            } catch (Exception e) {
                log.error("Failed to expire pending request {}", r.getId(), e);
            }
        }

        // 2. Expire READY_FOR_PICKUP requests after hold time
        List<EmergencyRequest> expiredPickup = requestRepository.findByStatusAndExpiresAtBefore("READY_FOR_PICKUP", now);
        for (EmergencyRequest r : expiredPickup) {
            try {
                log.info("Expiring self-pickup request: {}", r.getReferenceCode());
                requestStateMachine.transitionToExpiredPickup(r);
            } catch (Exception e) {
                log.error("Failed to expire pickup request {}", r.getId(), e);
            }
        }
    }
}
