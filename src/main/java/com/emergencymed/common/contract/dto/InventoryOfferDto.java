package com.emergencymed.common.contract.dto;

import java.math.BigDecimal;

public record InventoryOfferDto(
    Long inventoryId,
    Long pharmacyId,
    String pharmacyName,
    Long medicineId,
    String medicineName,
    int availableQuantity,
    BigDecimal unitPrice,
    Double distanceKm
) {}
