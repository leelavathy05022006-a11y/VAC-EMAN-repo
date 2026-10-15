package com.emergencymed.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RegisterPharmacyRequest(
        @NotBlank(message = "Pharmacy name is required")
        String name,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "Locality is required")
        String locality,

        String city,
        String postalCode,

        @NotBlank(message = "Contact phone is required")
        String contactPhone,

        @NotNull(message = "Latitude is required")
        BigDecimal latitude,

        @NotNull(message = "Longitude is required")
        BigDecimal longitude,

        String openingHours,
        Boolean deliveryAvailable,

        String licenseNumber,
        String website,
        String email,
        Boolean opens24x7,
        BigDecimal deliveryRadiusKm,
        BigDecimal deliveryFee
) {
    public RegisterPharmacyRequest(
            String name,
            String address,
            String locality,
            String city,
            String postalCode,
            String contactPhone,
            BigDecimal latitude,
            BigDecimal longitude,
            String openingHours,
            Boolean deliveryAvailable) {
        this(name, address, locality, city, postalCode, contactPhone, latitude, longitude, openingHours, deliveryAvailable, null, null, null, false, null, null);
    }
}
