package com.emergencymed.request.service.impl;

import com.emergencymed.common.contract.EmergencyRequestLookup;
import com.emergencymed.common.dto.EmergencyRequestDto;
import com.emergencymed.request.entity.EmergencyRequest;
import com.emergencymed.request.repository.EmergencyRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EmergencyRequestLookupImpl implements EmergencyRequestLookup {

    private final EmergencyRequestRepository requestRepository;

    public EmergencyRequestLookupImpl(EmergencyRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    public List<EmergencyRequestDto> findByRequesterUserId(Long userId) {
        return requestRepository.findByRequesterUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EmergencyRequestDto findByReferenceCode(String referenceCode) {
        return requestRepository.findByReferenceCode(referenceCode)
                .map(this::toDto)
                .orElse(null);
    }

    private EmergencyRequestDto toDto(EmergencyRequest req) {
        return new EmergencyRequestDto(
                req.getId(),
                req.getReferenceCode(),
                req.getRequesterUserId(),
                req.getPharmacyId(),
                "Pharmacy #" + req.getPharmacyId(),
                null,
                null,
                null,
                req.getStatus() != null ? req.getStatus() : "PENDING",
                req.getPaymentMode(),
                null,
                req.getHandoverCode(),
                req.getCreatedAt() != null ? req.getCreatedAt().toString() : "",
                req.getUpdatedAt() != null ? req.getUpdatedAt().toString() : ""
        );
    }
}
