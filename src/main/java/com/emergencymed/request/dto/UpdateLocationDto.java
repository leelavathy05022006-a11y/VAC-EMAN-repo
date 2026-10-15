package com.emergencymed.request.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class UpdateLocationDto {

    @NotNull(message = "Latitude is required")
    private BigDecimal lat;

    @NotNull(message = "Longitude is required")
    private BigDecimal lng;

    public UpdateLocationDto() {
    }

    public UpdateLocationDto(BigDecimal lat, BigDecimal lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public void setLng(BigDecimal lng) {
        this.lng = lng;
    }
}
