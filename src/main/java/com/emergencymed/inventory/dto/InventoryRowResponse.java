package com.emergencymed.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryRowResponse(
        Long id,
        Long pharmacyId,
        Long medicineId,
        String medicineName,
        String genericName,
        String strength,
        String dosageForm,
        String categoryName,
        Boolean requiresPrescription,
        String batchNo,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity,
        BigDecimal unitPrice,
        Integer lowStockThreshold,
        LocalDate expiryDate,
        String stockStatus,
        Boolean expiringWithin30Days,
        String dataSource,
        Boolean isActive,
        String lastUpdatedAt,
        String createdAt
) {}
