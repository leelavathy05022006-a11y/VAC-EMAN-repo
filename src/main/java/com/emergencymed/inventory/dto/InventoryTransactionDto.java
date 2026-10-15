package com.emergencymed.inventory.dto;

public record InventoryTransactionDto(
        Long id,
        Long inventoryId,
        String txnType,
        Integer quantityChange,
        Integer quantityAfter,
        Integer reservedAfter,
        String batchNo,
        String referenceCode,
        Long performedByUserId,
        String remarks,
        String createdAt
) {}
