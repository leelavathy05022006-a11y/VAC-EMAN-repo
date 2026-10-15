package com.emergencymed.inventory.dto;

import java.util.List;

public record PharmacyDiscoveryResponse(
        Double userLatitude,
        Double userLongitude,
        Double searchRadiusKm,
        String detectedLocality,
        String detectedAddress,
        Integer totalFound,
        String directoryCoverageNote,
        List<PharmacyDto> pharmacies
) {}
