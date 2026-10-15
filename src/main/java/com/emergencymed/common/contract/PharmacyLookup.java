package com.emergencymed.common.contract;

import com.emergencymed.common.dto.*;

import java.util.List;
import java.util.Optional;

public interface PharmacyLookup {
    Optional<PharmacyDto> findApproved(Long id);
    List<PharmacyDto> findApprovedWithin(double lat, double lng, double radiusKm);
    Optional<Long> pharmacyIdForUser(Long userId);
    
    default boolean isApproved(Long pharmacyId) {
        return findApproved(pharmacyId).map(PharmacyDto::isApproved).orElse(false);
    }
}
