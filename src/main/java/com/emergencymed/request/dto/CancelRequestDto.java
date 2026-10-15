package com.emergencymed.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CancelRequestDto {

    @NotBlank(message = "Cancellation reason is mandatory")
    @Size(min = 5, max = 255, message = "Cancellation reason must be at least 5 characters")
    private String reason;

    public CancelRequestDto() {
    }

    public CancelRequestDto(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
