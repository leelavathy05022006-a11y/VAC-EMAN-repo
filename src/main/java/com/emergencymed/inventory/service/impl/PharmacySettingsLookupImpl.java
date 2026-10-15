package com.emergencymed.inventory.service.impl;

import com.emergencymed.common.contract.PharmacySettingsLookup;
import com.emergencymed.common.dto.PharmacySettingsDto;
import com.emergencymed.inventory.repository.PharmacySettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PharmacySettingsLookupImpl implements PharmacySettingsLookup {
    private final PharmacySettingsRepository settingsRepository;

    public PharmacySettingsLookupImpl(PharmacySettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Override
    public Optional<PharmacySettingsDto> find(Long pharmacyId) {
        if (pharmacyId == null) {
            return Optional.empty();
        }
        return settingsRepository.findByPharmacyId(pharmacyId)
                .map(s -> new PharmacySettingsDto(
                        s.getPharmacyId(),
                        s.getDeliveryEnabled(),
                        s.getDeliveryRadiusKm(),
                        s.getDeliveryFee()
                ));
    }
}
