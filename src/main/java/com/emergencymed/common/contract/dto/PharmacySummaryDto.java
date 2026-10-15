package com.emergencymed.common.contract.dto;

import com.emergencymed.common.enums.ApprovalStatus;

public record PharmacySummaryDto(
    Long id,
    Long userId,
    String name,
    String licenseNo,
    String gstNo,
    String address,
    String city,
    String pincode,
    Double latitude,
    Double longitude,
    ApprovalStatus approvalStatus,
    Boolean isActive,
    Double distanceKm,
    String directionsUrl
) {}
