package com.emergencymed.common.contract.dto;

import java.math.BigDecimal;

public record PharmacySettingsDto(
    Long pharmacyId,
    boolean open24Hours,
    boolean homeDeliveryAvailable,
    BigDecimal minimumOrderAmount,
    double deliveryRadiusKm
) {}
