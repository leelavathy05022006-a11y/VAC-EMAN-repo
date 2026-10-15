package com.emergencymed.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MedicineDto(
        Long id,
        @NotNull(message = "Category ID is required")
        Long categoryId,
        String categoryName,
        @NotBlank(message = "Medicine name is required")
        String name,
        @NotBlank(message = "Generic name is required")
        String genericName,
        @NotBlank(message = "Strength is required")
        String strength,
        @NotBlank(message = "Dosage form is required")
        String dosageForm,
        @NotBlank(message = "Manufacturer is required")
        String manufacturer,
        Boolean requiresPrescription,
        Boolean isActive
) {}
