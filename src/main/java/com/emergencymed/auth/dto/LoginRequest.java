package com.emergencymed.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email address is required")
    String email,

    @NotBlank(message = "Password is required")
    String password
) {}
