package com.emergencymed.search.dto;

import java.math.BigDecimal;

public class DeliveryInfo {
    private String status; // "AVAILABLE", "BUSY", "UNAVAILABLE"
    private BigDecimal fee;
    private Double radiusKm;

    public DeliveryInfo() {
    }

    public DeliveryInfo(String status, BigDecimal fee, Double radiusKm) {
        this.status = status;
        this.fee = fee;
        this.radiusKm = radiusKm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public Double getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(Double radiusKm) {
        this.radiusKm = radiusKm;
    }
}
