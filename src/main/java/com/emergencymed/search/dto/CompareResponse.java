package com.emergencymed.search.dto;

import java.util.ArrayList;
import java.util.List;

public class CompareResponse {
    private List<PharmacyMatch> pharmacies = new ArrayList<>();
    private Long medicineId;
    private Integer quantity;

    public CompareResponse() {
    }

    public CompareResponse(List<PharmacyMatch> pharmacies, Long medicineId, Integer quantity) {
        this.pharmacies = pharmacies != null ? pharmacies : new ArrayList<>();
        this.medicineId = medicineId;
        this.quantity = quantity;
    }

    public List<PharmacyMatch> getPharmacies() {
        return pharmacies;
    }

    public void setPharmacies(List<PharmacyMatch> pharmacies) {
        this.pharmacies = pharmacies;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
