package com.emergencymed.request.service.impl;

import com.emergencymed.common.dto.PharmacySettingsDto;
import com.emergencymed.common.enums.DeliveryStatus;

import com.emergencymed.common.contract.*;
import com.emergencymed.request.entity.DeliveryAvailability;
import com.emergencymed.request.repository.DeliveryAvailabilityRepository;
import com.emergencymed.request.repository.EmergencyRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryAvailabilityService implements DeliveryAvailabilityQuery {

    private final DeliveryAvailabilityRepository availabilityRepository;
    private final EmergencyRequestRepository requestRepository;
    private final PharmacySettingsLookup settingsLookup;
    private final CurrentUserProvider currentUserProvider;

    public DeliveryAvailabilityService(
            DeliveryAvailabilityRepository availabilityRepository,
            EmergencyRequestRepository requestRepository,
            PharmacySettingsLookup settingsLookup,
            CurrentUserProvider currentUserProvider) {
        this.availabilityRepository = availabilityRepository;
        this.requestRepository = requestRepository;
        this.settingsLookup = settingsLookup;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryStatus statusFor(Long pharmacyId) {
        Optional<PharmacySettingsDto> settings = settingsLookup.find(pharmacyId);
        if (settings.isEmpty() || !settings.get().isDeliveryEnabled()) {
            return DeliveryStatus.UNAVAILABLE;
        }

        List<DeliveryAvailability> availablePartners = availabilityRepository.findByIsAvailableTrue();
        if (availablePartners.isEmpty()) {
            return DeliveryStatus.UNAVAILABLE;
        }

        boolean hasFreePartner = false;
        for (DeliveryAvailability partner : availablePartners) {
            boolean hasActiveJob = requestRepository.findActiveJobForPartner(partner.getPartnerUserId()).isPresent();
            if (!hasActiveJob) {
                hasFreePartner = true;
                break;
            }
        }

        return hasFreePartner ? DeliveryStatus.AVAILABLE : DeliveryStatus.BUSY;
    }

    @Transactional(readOnly = true)
    public DeliveryAvailability getMyAvailability() {
        Long partnerId = currentUserProvider.userId();
        return availabilityRepository.findByPartnerUserId(partnerId)
                .orElseGet(() -> new DeliveryAvailability(partnerId, false, null, null, null, LocalDateTime.now()));
    }

    @Transactional
    public DeliveryAvailability updateMyAvailability(boolean available, BigDecimal lat, BigDecimal lng) {
        Long partnerId = currentUserProvider.userId();
        DeliveryAvailability record = availabilityRepository.findByPartnerUserId(partnerId)
                .orElseGet(() -> new DeliveryAvailability(partnerId, available, lat, lng, LocalDateTime.now(), LocalDateTime.now()));

        record.setAvailable(available);
        if (lat != null && lng != null) {
            record.setCurrentLat(lat);
            record.setCurrentLng(lng);
            record.setLastLocationAt(LocalDateTime.now());
        }
        record.setUpdatedAt(LocalDateTime.now());
        return availabilityRepository.save(record);
    }

    @Transactional
    public DeliveryAvailability updateMyLocation(BigDecimal lat, BigDecimal lng) {
        Long partnerId = currentUserProvider.userId();
        DeliveryAvailability record = availabilityRepository.findByPartnerUserId(partnerId)
                .orElseGet(() -> new DeliveryAvailability(partnerId, true, lat, lng, LocalDateTime.now(), LocalDateTime.now()));

        record.setCurrentLat(lat);
        record.setCurrentLng(lng);
        record.setLastLocationAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        return availabilityRepository.save(record);
    }
}
