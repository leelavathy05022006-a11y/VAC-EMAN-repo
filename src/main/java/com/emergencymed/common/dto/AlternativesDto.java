package com.emergencymed.common.dto;

import java.util.ArrayList;
import java.util.List;

public class AlternativesDto {
    private List<MedicineDto> genericSubstitutes = new ArrayList<>();
    private List<MedicineOfferDto> partialStockOffers = new ArrayList<>();

    public AlternativesDto() {
    }

    public AlternativesDto(List<MedicineDto> genericSubstitutes, List<MedicineOfferDto> partialStockOffers) {
        this.genericSubstitutes = genericSubstitutes != null ? genericSubstitutes : new ArrayList<>();
        this.partialStockOffers = partialStockOffers != null ? partialStockOffers : new ArrayList<>();
    }

    public List<MedicineDto> getGenericSubstitutes() {
        return genericSubstitutes;
    }

    public void setGenericSubstitutes(List<MedicineDto> genericSubstitutes) {
        this.genericSubstitutes = genericSubstitutes;
    }

    public List<MedicineOfferDto> getPartialStockOffers() {
        return partialStockOffers;
    }

    public void setPartialStockOffers(List<MedicineOfferDto> partialStockOffers) {
        this.partialStockOffers = partialStockOffers;
    }
}
