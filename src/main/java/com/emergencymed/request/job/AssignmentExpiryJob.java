package com.emergencymed.request.job;

import com.emergencymed.request.entity.DeliveryAssignment;
import com.emergencymed.request.repository.DeliveryAssignmentRepository;
import com.emergencymed.request.service.DeliveryAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AssignmentExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(AssignmentExpiryJob.class);

    private final DeliveryAssignmentRepository assignmentRepository;
    private final DeliveryAssignmentService deliveryAssignmentService;

    public AssignmentExpiryJob(
            DeliveryAssignmentRepository assignmentRepository,
            DeliveryAssignmentService deliveryAssignmentService) {
        this.assignmentRepository = assignmentRepository;
        this.deliveryAssignmentService = deliveryAssignmentService;
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        List<DeliveryAssignment> expiredOffers = assignmentRepository.findByStatusAndExpiresAtBefore("OFFERED", now);

        for (DeliveryAssignment assignment : expiredOffers) {
            try {
                log.info("Expiring delivery offer {} for request {}", assignment.getId(), assignment.getRequestId());
                deliveryAssignmentService.expireOffer(assignment);
            } catch (Exception e) {
                log.error("Failed to expire delivery offer {}", assignment.getId(), e);
            }
        }
    }
}
