package com.emergencymed.request.repository;

import com.emergencymed.request.entity.EmergencyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long>, JpaSpecificationExecutor<EmergencyRequest> {

    Optional<EmergencyRequest> findByReferenceCode(String referenceCode);

    Page<EmergencyRequest> findByRequesterUserId(Long requesterUserId, Pageable pageable);
    List<EmergencyRequest> findByRequesterUserIdOrderByCreatedAtDesc(Long requesterUserId);

    Page<EmergencyRequest> findByRequesterUserIdAndStatusIn(Long requesterUserId, Collection<String> statuses, Pageable pageable);

    Page<EmergencyRequest> findByPharmacyId(Long pharmacyId, Pageable pageable);

    Page<EmergencyRequest> findByPharmacyIdAndStatusIn(Long pharmacyId, Collection<String> statuses, Pageable pageable);

    List<EmergencyRequest> findByStatusAndExpiresAtBefore(String status, LocalDateTime threshold);

    Optional<EmergencyRequest> findTopByRequesterUserIdAndPharmacyIdAndCreatedAtAfterOrderByCreatedAtDesc(
            Long requesterUserId, Long pharmacyId, LocalDateTime threshold);

    long countByStatus(String status);

    long countByPharmacyIdAndStatus(Long pharmacyId, String status);

    long countByRequesterUserIdAndStatus(Long requesterUserId, String status);

    long countByDeliveryPartnerUserIdAndStatus(Long deliveryPartnerUserId, String status);

    @Query("SELECT r FROM EmergencyRequest r WHERE r.deliveryPartnerUserId = :partnerId AND r.status IN :statuses")
    Page<EmergencyRequest> findByDeliveryPartnerAndStatusIn(@Param("partnerId") Long partnerId,
                                                           @Param("statuses") Collection<String> statuses,
                                                           Pageable pageable);

    @Query("SELECT r FROM EmergencyRequest r WHERE r.deliveryPartnerUserId = :partnerId AND r.status IN ('DELIVERY_ASSIGNED', 'OUT_FOR_DELIVERY')")
    Optional<EmergencyRequest> findActiveJobForPartner(@Param("partnerId") Long partnerId);
}
