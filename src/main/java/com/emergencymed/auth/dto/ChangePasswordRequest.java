package com.emergencymed.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
    @NotBlank(message = "Current password is required")
    String oldPassword,

    @NotBlank(message = "New password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,64}$",
        message = "New password must be 8-64 characters and contain uppercase, lowercase, number, and special character"
    )
    String newPassword,

    @NotBlank(message = "Password confirmation is required")
    String confirmPassword
) {}
