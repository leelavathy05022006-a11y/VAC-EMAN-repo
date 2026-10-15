package com.emergencymed.inventory.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record StockInRequest(
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than 0")
        @Max(value = 100000, message = "Quantity must be at most 100000")
        Integer quantity,

        @Pattern(regexp = "^[A-Za-z0-9-]{3,40}$", message = "Batch number must be 3 to 40 alphanumeric characters or hyphens")
        String batchNo,

        @Future(message = "Expiry date must be after today")
        LocalDate expiryDate
) {}
