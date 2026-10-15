package com.emergencymed.search.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ContactLogRequest {

    @NotNull(message = "Pharmacy ID is required")
    private Long pharmacyId;

    private Long medicineId;

    @NotBlank(message = "Contact type is required")
    @Pattern(regexp = "CALL|WHATSAPP|NAVIGATION", message = "Contact type must be one of CALL, WHATSAPP, or NAVIGATION")
    private String contactType;

    public ContactLogRequest() {
    }

    public ContactLogRequest(Long pharmacyId, Long medicineId, String contactType) {
        this.pharmacyId = pharmacyId;
        this.medicineId = medicineId;
        this.contactType = contactType;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }
}
