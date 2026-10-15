package com.emergencymed.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejectRequestDto {

    @NotBlank(message = "Rejection reason is mandatory")
    @Size(min = 10, max = 255, message = "Rejection reason must be at least 10 characters")
    private String reason;

    public RejectRequestDto() {
    }

    public RejectRequestDto(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
