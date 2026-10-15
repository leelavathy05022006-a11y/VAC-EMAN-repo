package com.emergencymed.search.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SearchRequest {

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be between 1 and 1000")
    @Max(value = 1000, message = "Quantity must be between 1 and 1000")
    private Integer quantity;

    @NotNull(message = "Location is required")
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    private Double lat;

    @NotNull(message = "Location is required")
    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    private Double lng;

    @Min(value = 1, message = "Radius must be between 1 and 50 km")
    @Max(value = 50, message = "Radius must be between 1 and 50 km")
    private Double radiusKm = 10.0;

    private String sort = "distance"; // "distance", "price", "quantity", "delivery"
    private Boolean deliveryOnly = false;
    private Integer page = 0;
    private Integer size = 10;

    public SearchRequest() {
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

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(Double radiusKm) {
        this.radiusKm = radiusKm;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Boolean getDeliveryOnly() {
        return deliveryOnly;
    }

    public void setDeliveryOnly(Boolean deliveryOnly) {
        this.deliveryOnly = deliveryOnly;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
