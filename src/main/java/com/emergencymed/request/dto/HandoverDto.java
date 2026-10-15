package com.emergencymed.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class HandoverDto {

    @NotBlank(message = "Handover code is required")
    @Pattern(regexp = "^[0-9]{4}$", message = "Handover code must be exactly 4 digits")
    private String handoverCode;

    public HandoverDto() {
    }

    public HandoverDto(String handoverCode) {
        this.handoverCode = handoverCode;
    }

    public String getHandoverCode() {
        return handoverCode;
    }

    public void setHandoverCode(String handoverCode) {
        this.handoverCode = handoverCode;
    }
}
