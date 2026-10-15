package com.emergencymed.request.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OfferResponse {
    private Long assignmentId;
    private Long requestId;
    private String referenceCode;
    private String priority;
    private String pharmacyName;
    private String pharmacyAddress;
    private BigDecimal pharmacyLat;
    private BigDecimal pharmacyLng;
    private String deliveryAddress;
    private BigDecimal deliveryLat;
    private BigDecimal deliveryLng;
    private BigDecimal deliveryFee;
    private LocalDateTime offeredAt;
    private LocalDateTime expiresAt;
    private long secondsRemaining;

    public OfferResponse() {
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public String getPharmacyAddress() {
        return pharmacyAddress;
    }

    public void setPharmacyAddress(String pharmacyAddress) {
        this.pharmacyAddress = pharmacyAddress;
    }

    public BigDecimal getPharmacyLat() {
        return pharmacyLat;
    }

    public void setPharmacyLat(BigDecimal pharmacyLat) {
        this.pharmacyLat = pharmacyLat;
    }

    public BigDecimal getPharmacyLng() {
        return pharmacyLng;
    }

    public void setPharmacyLng(BigDecimal pharmacyLng) {
        this.pharmacyLng = pharmacyLng;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public BigDecimal getDeliveryLat() {
        return deliveryLat;
    }

    public void setDeliveryLat(BigDecimal deliveryLat) {
        this.deliveryLat = deliveryLat;
    }

    public BigDecimal getDeliveryLng() {
        return deliveryLng;
    }

    public void setDeliveryLng(BigDecimal deliveryLng) {
        this.deliveryLng = deliveryLng;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public LocalDateTime getOfferedAt() {
        return offeredAt;
    }

    public void setOfferedAt(LocalDateTime offeredAt) {
        this.offeredAt = offeredAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getSecondsRemaining() {
        return secondsRemaining;
    }

    public void setSecondsRemaining(long secondsRemaining) {
        this.secondsRemaining = secondsRemaining;
    }
}
