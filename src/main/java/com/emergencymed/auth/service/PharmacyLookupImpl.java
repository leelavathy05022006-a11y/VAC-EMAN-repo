package com.emergencymed.auth.service;

import com.emergencymed.common.dto.PharmacyDto;

import com.emergencymed.auth.entity.Pharmacy;
import com.emergencymed.auth.repository.PharmacyRepository;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.common.enums.ApprovalStatus;
import com.emergencymed.common.util.GeoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PharmacyLookupImpl implements PharmacyLookup {

    private final PharmacyRepository pharmacyRepository;

    public PharmacyLookupImpl(PharmacyRepository pharmacyRepository) {
        this.pharmacyRepository = pharmacyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PharmacyDto> findApproved(Long id) {
        return pharmacyRepository.findById(id)
                .filter(p -> (p.getApprovalStatus() == ApprovalStatus.APPROVED || Boolean.TRUE.equals(p.getIsApproved())) && p.isActive())
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyDto> findApprovedWithin(double lat, double lng, double radiusKm) {
        double[] box = GeoUtils.boundingBox(lat, lng, radiusKm);
        List<Pharmacy> candidateList = pharmacyRepository
                .findByLatitudeBetweenAndLongitudeBetweenAndApprovalStatusAndIsActiveTrue(
                        box[0], box[1], box[2], box[3], ApprovalStatus.APPROVED
                );

        return candidateList.stream()
                .map(p -> new PharmacyWithDistance(p, GeoUtils.haversineKm(lat, lng, p.getLatitude(), p.getLongitude())))
                .filter(pwd -> pwd.distance <= radiusKm)
                .sorted(Comparator.comparingDouble(pwd -> pwd.distance))
                .map(pwd -> toDto(pwd.pharmacy))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> pharmacyIdForUser(Long userId) {
        return pharmacyRepository.findByUserId(userId).map(Pharmacy::getId);
    }

    private record PharmacyWithDistance(Pharmacy pharmacy, double distance) {}

    private PharmacyDto toDto(Pharmacy p) {
        Long userId = p.getUser() != null ? p.getUser().getId() : null;
        return new PharmacyDto(
                p.getId(),
                userId,
                p.getName(),
                p.getAddress(),
                p.getLatitude() != null ? BigDecimal.valueOf(p.getLatitude()) : BigDecimal.ZERO,
                p.getLongitude() != null ? BigDecimal.valueOf(p.getLongitude()) : BigDecimal.ZERO,
                p.getContactPhone() != null ? p.getContactPhone() : (p.getPhoneNumber() != null ? p.getPhoneNumber() : ""),
                p.getLicenseNo() != null ? p.getLicenseNo() : (p.getLicenseNumber() != null ? p.getLicenseNumber() : ""),
                p.getApprovalStatus() == ApprovalStatus.APPROVED || Boolean.TRUE.equals(p.getIsApproved())
        );
    }
}
