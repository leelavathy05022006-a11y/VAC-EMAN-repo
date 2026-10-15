package com.emergencymed.request.dto;

import java.math.BigDecimal;

public class UpdateAvailabilityDto {
    private boolean available;
    private BigDecimal lat;
    private BigDecimal lng;

    public UpdateAvailabilityDto() {
    }

    public UpdateAvailabilityDto(boolean available, BigDecimal lat, BigDecimal lng) {
        this.available = available;
        this.lat = lat;
        this.lng = lng;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
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
