package com.emergencymed.search.dto;

import com.emergencymed.common.dto.MedicineDto;

import java.util.ArrayList;
import java.util.List;

public class AlternativesResponse {
    private String message = "No exact matches found for requested quantity and radius. Showing recommendations:";
    private List<PharmacyMatch> widerRadiusMatches = new ArrayList<>();
    private List<PharmacyMatch> partialStockMatches = new ArrayList<>();
    private List<MedicineDto> genericSubstitutes = new ArrayList<>();

    public AlternativesResponse() {
    }

    public AlternativesResponse(String message, List<PharmacyMatch> widerRadiusMatches, List<PharmacyMatch> partialStockMatches, List<MedicineDto> genericSubstitutes) {
        this.message = message;
        this.widerRadiusMatches = widerRadiusMatches != null ? widerRadiusMatches : new ArrayList<>();
        this.partialStockMatches = partialStockMatches != null ? partialStockMatches : new ArrayList<>();
        this.genericSubstitutes = genericSubstitutes != null ? genericSubstitutes : new ArrayList<>();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<PharmacyMatch> getWiderRadiusMatches() {
        return widerRadiusMatches;
    }

    public void setWiderRadiusMatches(List<PharmacyMatch> widerRadiusMatches) {
        this.widerRadiusMatches = widerRadiusMatches;
    }

    public List<PharmacyMatch> getPartialStockMatches() {
        return partialStockMatches;
    }

    public void setPartialStockMatches(List<PharmacyMatch> partialStockMatches) {
        this.partialStockMatches = partialStockMatches;
    }

    public List<MedicineDto> getGenericSubstitutes() {
        return genericSubstitutes;
    }

    public void setGenericSubstitutes(List<MedicineDto> genericSubstitutes) {
        this.genericSubstitutes = genericSubstitutes;
    }
}
