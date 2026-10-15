package com.emergencymed.common.contract;

import com.emergencymed.common.dto.EmergencyRequestDto;

import java.util.List;

public interface EmergencyRequestLookup {
    List<EmergencyRequestDto> findByRequesterUserId(Long userId);
    EmergencyRequestDto findByReferenceCode(String referenceCode);

    default EmergencyRequestDto createRequest(EmergencyRequestDto request) {
        return null;
    }
}

