package com.emergencymed.request.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LiveTrackingResponse {

    private Long requestId;
    private String referenceCode;
    private String status;
    private String fulfilmentType;
    private boolean trackingActive;

    // Delivery Partner info
    private Long deliveryPartnerUserId;
    private String deliveryPartnerName;
    private String deliveryPartnerPhone;
    private BigDecimal partnerLat;
    private BigDecimal partnerLng;
    private LocalDateTime lastLocationAt;

    // Pharmacy info
    private Long pharmacyId;
    private String pharmacyName;
    private String pharmacyAddress;
    private String pharmacyPhone;
    private BigDecimal pharmacyLat;
    private BigDecimal pharmacyLng;

    // Destination info
    private String deliveryAddress;
    private BigDecimal deliveryLat;
    private BigDecimal deliveryLng;

    // Calculated metrics
    private Double remainingDistanceKm;
    private Integer estimatedArrivalMinutes;
    private String statusMessage;

    public LiveTrackingResponse() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFulfilmentType() {
        return fulfilmentType;
    }

    public void setFulfilmentType(String fulfilmentType) {
        this.fulfilmentType = fulfilmentType;
    }

    public boolean isTrackingActive() {
        return trackingActive;
    }

    public void setTrackingActive(boolean trackingActive) {
        this.trackingActive = trackingActive;
    }

    public Long getDeliveryPartnerUserId() {
        return deliveryPartnerUserId;
    }

    public void setDeliveryPartnerUserId(Long deliveryPartnerUserId) {
        this.deliveryPartnerUserId = deliveryPartnerUserId;
    }

    public String getDeliveryPartnerName() {
        return deliveryPartnerName;
    }

    public void setDeliveryPartnerName(String deliveryPartnerName) {
        this.deliveryPartnerName = deliveryPartnerName;
    }

    public String getDeliveryPartnerPhone() {
        return deliveryPartnerPhone;
    }

    public void setDeliveryPartnerPhone(String deliveryPartnerPhone) {
        this.deliveryPartnerPhone = deliveryPartnerPhone;
    }

    public BigDecimal getPartnerLat() {
        return partnerLat;
    }

    public void setPartnerLat(BigDecimal partnerLat) {
        this.partnerLat = partnerLat;
    }

    public BigDecimal getPartnerLng() {
        return partnerLng;
    }

    public void setPartnerLng(BigDecimal partnerLng) {
        this.partnerLng = partnerLng;
    }

    public LocalDateTime getLastLocationAt() {
        return lastLocationAt;
    }

    public void setLastLocationAt(LocalDateTime lastLocationAt) {
        this.lastLocationAt = lastLocationAt;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
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

    public String getPharmacyPhone() {
        return pharmacyPhone;
    }

    public void setPharmacyPhone(String pharmacyPhone) {
        this.pharmacyPhone = pharmacyPhone;
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

    public Double getRemainingDistanceKm() {
        return remainingDistanceKm;
    }

    public void setRemainingDistanceKm(Double remainingDistanceKm) {
        this.remainingDistanceKm = remainingDistanceKm;
    }

    public Integer getEstimatedArrivalMinutes() {
        return estimatedArrivalMinutes;
    }

    public void setEstimatedArrivalMinutes(Integer estimatedArrivalMinutes) {
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
