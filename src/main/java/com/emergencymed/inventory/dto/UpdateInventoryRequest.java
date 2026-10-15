package com.emergencymed.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateInventoryRequest(
        @Pattern(regexp = "^[A-Za-z0-9-]{3,40}$", message = "Batch number must be 3 to 40 alphanumeric characters or hyphens")
        String batchNo,

        @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
        @DecimalMax(value = "100000.00", message = "Unit price must be at most 100000")
        BigDecimal unitPrice,

        @Min(value = 0, message = "Low stock threshold must be between 0 and 1000")
        @Max(value = 1000, message = "Low stock threshold must be between 0 and 1000")
        Integer lowStockThreshold,

        @Future(message = "Expiry date must be after today")
        LocalDate expiryDate
) {}
