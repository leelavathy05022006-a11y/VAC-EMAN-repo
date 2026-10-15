package com.emergencymed.inventory.service;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.inventory.dto.PharmacySettingsDto;
import com.emergencymed.inventory.entity.PharmacySettings;
import com.emergencymed.inventory.exception.ApiException;
import com.emergencymed.inventory.repository.PharmacySettingsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;

@Service
@Transactional
public class PharmacySettingsService {
    private final PharmacySettingsRepository settingsRepository;
    private final CurrentUserProvider currentUserProvider;

    public PharmacySettingsService(PharmacySettingsRepository settingsRepository, CurrentUserProvider currentUserProvider) {
        this.settingsRepository = settingsRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public PharmacySettings getOrCreateSettings(Long pharmacyId) {
        return settingsRepository.findByPharmacyId(pharmacyId)
                .orElseGet(() -> {
                    PharmacySettings newSettings = new PharmacySettings(pharmacyId);
                    newSettings.setDeliveryEnabled(false);
                    newSettings.setDeliveryRadiusKm(new BigDecimal("5.0"));
                    newSettings.setDeliveryFee(new BigDecimal("20.00"));
                    newSettings.setOpens24x7(true);
                    newSettings.setOpeningTime(LocalTime.of(8, 0));
                    newSettings.setClosingTime(LocalTime.of(22, 0));
                    newSettings.setAcceptsEmergencyOrders(true);
                    return settingsRepository.save(newSettings);
                });
    }

    public PharmacySettingsDto getCurrentPharmacySettings() {
        Long pharmacyId = currentUserProvider.pharmacyId()
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Session does not contain pharmacy ID"));
        PharmacySettings settings = getOrCreateSettings(pharmacyId);
        return toDto(settings);
    }

    public PharmacySettingsDto getSettingsByPharmacyId(Long pharmacyId) {
        PharmacySettings settings = getOrCreateSettings(pharmacyId);
        return toDto(settings);
    }

    public PharmacySettingsDto updateCurrentPharmacySettings(PharmacySettingsDto dto) {
        Long pharmacyId = currentUserProvider.pharmacyId()
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Session does not contain pharmacy ID"));

        validateSettings(dto);

        PharmacySettings settings = getOrCreateSettings(pharmacyId);
        if (dto.contactPhone() != null) settings.setContactPhone(dto.contactPhone());
        if (dto.whatsappNumber() != null) settings.setWhatsappNumber(dto.whatsappNumber());
        if (dto.deliveryEnabled() != null) settings.setDeliveryEnabled(dto.deliveryEnabled());
        if (dto.deliveryRadiusKm() != null) settings.setDeliveryRadiusKm(dto.deliveryRadiusKm());
        if (dto.deliveryFee() != null) settings.setDeliveryFee(dto.deliveryFee());
        if (dto.opens24x7() != null) settings.setOpens24x7(dto.opens24x7());
        if (dto.openingTime() != null) settings.setOpeningTime(dto.openingTime());
        if (dto.closingTime() != null) settings.setClosingTime(dto.closingTime());
        if (dto.acceptsEmergencyOrders() != null) settings.setAcceptsEmergencyOrders(dto.acceptsEmergencyOrders());

        PharmacySettings saved = settingsRepository.save(settings);
        return toDto(saved);
    }

    private void validateSettings(PharmacySettingsDto dto) {
        if (dto.whatsappNumber() != null && dto.whatsappNumber().contains("+")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "WhatsApp number must not contain '+'");
        }
        if (Boolean.FALSE.equals(dto.opens24x7())) {
            if (dto.openingTime() != null && dto.closingTime() != null) {
                if (!dto.closingTime().isAfter(dto.openingTime())) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Closing time must be after opening time unless opens 24x7");
                }
            }
        }
    }

    public PharmacySettingsDto toDto(PharmacySettings s) {
        return new PharmacySettingsDto(
                s.getPharmacyId(),
                s.getContactPhone(),
                s.getWhatsappNumber(),
                s.getDeliveryEnabled(),
                s.getDeliveryRadiusKm(),
                s.getDeliveryFee(),
                s.getOpens24x7(),
                s.getOpeningTime(),
                s.getClosingTime(),
                s.getAcceptsEmergencyOrders()
        );
    }
}
