package com.emergencymed.common.events;

public record PharmacyRejectedEvent(
    Long pharmacyId,
    Long userId,
    String pharmacyName,
    String reason
) {}
