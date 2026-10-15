package com.emergencymed.auth.dto;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
    @NotNull(message = "Active status is required")
    Boolean active
) {}
