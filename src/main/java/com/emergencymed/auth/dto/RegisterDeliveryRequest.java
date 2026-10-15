package com.emergencymed.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDeliveryRequest(
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    String fullName,

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
    String confirmPassword,

    String vehicleType,
    String vehicleNumber,

    @NotBlank(message = "Driving licence number is required")
    @Size(min = 5, max = 30, message = "Driving licence must be between 5 and 30 characters")
    String drivingLicenseNo,

    String city
) {}
