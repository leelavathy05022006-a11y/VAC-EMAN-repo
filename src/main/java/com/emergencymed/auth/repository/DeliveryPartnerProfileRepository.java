package com.emergencymed.auth.repository;

import com.emergencymed.auth.entity.DeliveryPartnerProfile;
import com.emergencymed.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerProfileRepository extends JpaRepository<DeliveryPartnerProfile, Long> {
    Optional<DeliveryPartnerProfile> findByUserId(Long userId);
    Optional<DeliveryPartnerProfile> findByUser(User user);
    long countByIsVerifiedFalse();

    @Query("SELECT d.user.id FROM DeliveryPartnerProfile d WHERE d.isVerified = true AND d.user.isActive = true")
    List<Long> findActiveAndVerifiedDeliveryPartnerUserIds();
}
