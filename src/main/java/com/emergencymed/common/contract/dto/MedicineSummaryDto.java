package com.emergencymed.common.contract.dto;

public record MedicineSummaryDto(
    Long id,
    String name,
    String genericName,
    String category,
    String dosageForm,
    String strength,
    boolean requiresPrescription
) {}
