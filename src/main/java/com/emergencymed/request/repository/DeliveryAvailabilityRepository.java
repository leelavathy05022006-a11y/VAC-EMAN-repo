package com.emergencymed.request.repository;

import com.emergencymed.request.entity.DeliveryAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAvailabilityRepository extends JpaRepository<DeliveryAvailability, Long> {
    Optional<DeliveryAvailability> findByPartnerUserId(Long partnerUserId);
    List<DeliveryAvailability> findByIsAvailableTrue();
}
