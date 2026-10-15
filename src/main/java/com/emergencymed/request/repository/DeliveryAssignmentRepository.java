package com.emergencymed.request.repository;

import com.emergencymed.request.entity.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {

    List<DeliveryAssignment> findByRequestIdOrderByAttemptNoAsc(Long requestId);

    Optional<DeliveryAssignment> findTopByRequestIdOrderByAttemptNoDesc(Long requestId);

    List<DeliveryAssignment> findByPartnerUserIdAndStatus(Long partnerUserId, String status);

    Optional<DeliveryAssignment> findByIdAndPartnerUserId(Long id, Long partnerUserId);

    List<DeliveryAssignment> findByStatusAndExpiresAtBefore(String status, LocalDateTime threshold);
}
