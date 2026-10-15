package com.emergencymed.inventory.repository;

import com.emergencymed.inventory.entity.PharmacySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PharmacySettingsRepository extends JpaRepository<PharmacySettings, Long> {
    Optional<PharmacySettings> findByPharmacyId(Long pharmacyId);
    boolean existsByPharmacyId(Long pharmacyId);
}
