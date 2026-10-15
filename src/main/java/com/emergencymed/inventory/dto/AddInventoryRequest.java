package com.emergencymed.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AddInventoryRequest(
        @NotNull(message = "Medicine ID is required")
        Long medicineId,

        @NotNull(message = "Batch number is required")
        @Pattern(regexp = "^[A-Za-z0-9-]{3,40}$", message = "Batch number must be 3 to 40 alphanumeric characters or hyphens")
        String batchNo,

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity must be between 0 and 100000")
        @Max(value = 100000, message = "Quantity must be between 0 and 100000")
        Integer quantity,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
        @DecimalMax(value = "100000.00", message = "Unit price must be at most 100000")
        BigDecimal unitPrice,

        @Min(value = 0, message = "Low stock threshold must be between 0 and 1000")
        @Max(value = 1000, message = "Low stock threshold must be between 0 and 1000")
        Integer lowStockThreshold,

        @NotNull(message = "Expiry date is required")
        @Future(message = "Expiry date must be after today")
        LocalDate expiryDate
) {}
