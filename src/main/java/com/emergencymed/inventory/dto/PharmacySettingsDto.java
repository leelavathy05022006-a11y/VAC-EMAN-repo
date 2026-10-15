package com.emergencymed.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalTime;

public record PharmacySettingsDto(
        Long pharmacyId,
        @Pattern(regexp = "^[0-9]{10,15}$", message = "Contact phone must be between 10 and 15 digits")
        String contactPhone,

        @Pattern(regexp = "^[0-9]{11,13}$", message = "WhatsApp number must be 11 to 13 digits including country code without +")
        String whatsappNumber,

        Boolean deliveryEnabled,

        @DecimalMin(value = "1.0", message = "Delivery radius must be between 1 and 50 km")
        @DecimalMax(value = "50.0", message = "Delivery radius must be between 1 and 50 km")
        BigDecimal deliveryRadiusKm,

        @DecimalMin(value = "0.0", message = "Delivery fee must be between 0 and 500")
        @DecimalMax(value = "500.0", message = "Delivery fee must be between 0 and 500")
        BigDecimal deliveryFee,

        Boolean opens24x7,
        LocalTime openingTime,
        LocalTime closingTime,
        Boolean acceptsEmergencyOrders
) {}
