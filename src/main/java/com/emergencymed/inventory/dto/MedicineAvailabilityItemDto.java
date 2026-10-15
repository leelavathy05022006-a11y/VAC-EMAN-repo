package com.emergencymed.inventory.dto;

import java.math.BigDecimal;

public record MedicineAvailabilityItemDto(
        Long inventoryId,
        Long medicineId,
        String medicineName,
        String genericName,
        String strength,
        String dosageForm,
        String manufacturer,
        String categoryName,
        Boolean requiresPrescription,
        Integer availableQuantity,
        Integer totalQuantity,
        Integer reservedQuantity,
        BigDecimal unitPrice,
        String batchNo,
        String expiryDate,
        Boolean isExpired,
        Boolean isNearExpiry,
        String stockStatus,
        String dataSource,
        String lastUpdatedAt
) {}
