package com.emergencymed.inventory.dto;

public record GeocodeSuggestionDto(
        String name,
        String locality,
        String city,
        String postalCode,
        Double latitude,
        Double longitude,
        String formattedAddress
) {}
