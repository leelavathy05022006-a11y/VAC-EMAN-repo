package com.emergencymed.auth.dto;

import jakarta.validation.constraints.*;

public record RegisterPharmacyRequest(
    @NotBlank(message = "Pharmacy name is required")
    @Size(max = 150, message = "Pharmacy name must not exceed 150 characters")
    String pharmacyName,

    @NotBlank(message = "Licence number is required")
    @Size(min = 5, max = 50, message = "Licence number must be between 5 and 50 characters")
    String licenseNo,

    @Size(max = 20, message = "GST number must not exceed 20 characters")
    String gstNo,

    @NotBlank(message = "Address is required")
    String address,

    String city,

    @Pattern(regexp = "^$|^[0-9]{6}$", message = "Pincode must be 6 digits")
    String pincode,

    @NotNull(message = "Latitude is required for pharmacies")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    Double latitude,

    @NotNull(message = "Longitude is required for pharmacies")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    Double longitude,

    @NotBlank(message = "Contact person name is required")
    String contactPersonName,

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    String email,

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Phone must be a valid 10-digit number")
    String phone,

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,64}$",
        message = "Password must be 8-64 characters and contain uppercase, lowercase, number, and special character"
    )
    String password,

    @NotBlank(message = "Password confirmation is required")
    String confirmPassword
) {}
