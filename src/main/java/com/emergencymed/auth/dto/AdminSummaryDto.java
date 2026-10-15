package com.emergencymed.auth.dto;

public record AdminSummaryDto(
    long totalUsers,
    long patientsCount,
    long hospitalsCount,
    long pharmaciesCount,
    long pendingPharmaciesCount,
    long approvedPharmaciesCount,
    long deliveryPartnersCount,
    long unverifiedDeliveryPartnersCount
) {}
