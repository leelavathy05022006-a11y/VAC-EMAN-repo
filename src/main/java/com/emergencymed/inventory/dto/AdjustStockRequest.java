package com.emergencymed.inventory.dto;

import jakarta.validation.constraints.*;

public record AdjustStockRequest(
        @NotNull(message = "New quantity is required")
        @Min(value = 0, message = "Quantity must be between 0 and 100000")
        @Max(value = 100000, message = "Quantity must be between 0 and 100000")
        Integer newQuantity,

        @NotBlank(message = "Adjustment reason is required")
        @Size(min = 5, max = 255, message = "Adjustment reason must be at least 5 characters")
        String reason
) {}
