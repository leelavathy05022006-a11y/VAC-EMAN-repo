package com.emergencymed.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectSuspendRequest(
    @NotBlank(message = "Reason is mandatory")
    @Size(min = 10, max = 255, message = "Reason must be at least 10 characters")
    String reason
) {}
