package com.emergencymed.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VerifyPrescriptionDto {

    @NotBlank(message = "Verification status is required")
    @Pattern(regexp = "^(VERIFIED|REJECTED)$", message = "Status must be VERIFIED or REJECTED")
    private String status;

    @Size(max = 255, message = "Remarks cannot exceed 255 characters")
    private String remarks;

    public VerifyPrescriptionDto() {
    }

    public VerifyPrescriptionDto(String status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
