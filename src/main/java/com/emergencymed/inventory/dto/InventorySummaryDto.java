package com.emergencymed.inventory.dto;

public record InventorySummaryDto(
        long totalItems,
        long inStockCount,
        long lowStockCount,
        long outOfStockCount,
        long expiringCount
) {}
