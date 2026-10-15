package com.emergencymed.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterHospitalRequest(
    @NotBlank(message = "Hospital name is required")
    @Size(max = 150, message = "Hospital name must not exceed 150 characters")
    String hospitalName,

    @NotBlank(message = "Registration number is required")
    @Size(min = 5, max = 50, message = "Registration number must be between 5 and 50 characters")
    String registrationNo,

    @NotBlank(message = "Emergency contact phone is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Emergency phone must be a valid 10-digit number")
    String emergencyPhone,

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    String email,

    @NotBlank(message = "Authorized person phone is required")
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

    String hospitalType,
    String address,
    String city,

    @Pattern(regexp = "^$|^[0-9]{6}$", message = "Pincode must be 6 digits")
    String pincode,

    Double latitude,
    Double longitude
) {}
