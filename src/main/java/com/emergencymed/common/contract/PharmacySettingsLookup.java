package com.emergencymed.common.contract;

import com.emergencymed.common.dto.*;

import java.util.Optional;

public interface PharmacySettingsLookup {
    Optional<PharmacySettingsDto> find(Long pharmacyId);
}
