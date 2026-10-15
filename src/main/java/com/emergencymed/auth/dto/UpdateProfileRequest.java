package com.emergencymed.auth.dto;

import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequest(
    String fullName,

    @Pattern(regexp = "^$|^[6-9][0-9]{9}$", message = "Phone must be a valid 10-digit number")
    String phone,

    // Patient fields
    String dateOfBirth,
    String gender,
    String bloodGroup,
    String emergencyContactName,
    String emergencyContactPhone,

    // Hospital fields
    String hospitalName,
    String hospitalType,
    String emergencyPhone,

    // Delivery fields
    String vehicleType,
    String vehicleNumber,
    String drivingLicenseNo,
    String serviceCity
) {}
