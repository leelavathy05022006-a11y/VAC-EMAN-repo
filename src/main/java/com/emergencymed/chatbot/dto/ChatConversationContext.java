package com.emergencymed.chatbot.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ChatConversationContext {
    private String sessionId;
    private Long userId;

    // Health memory
    private String lastSymptom;
    private String lastDiscussedMedicine;
    private Long lastDiscussedMedicineId;
    private String lastLocality;
    private Double lastLat;
    private Double lastLng;

    // Active conversational order draft
    private Long draftPharmacyId;
    private String draftPharmacyName;
    private Long draftMedicineId;
    private String draftMedicineName;
    private Integer draftQuantity = 1;
    private BigDecimal draftUnitPrice;
    private BigDecimal draftDeliveryFee = BigDecimal.ZERO;
    private String draftFulfilmentType; // "DELIVERY" or "SELF_PICKUP"
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String landmark;
    private boolean awaitingOrderConfirmation = false;
    private String awaitingField; // "PHARMACY", "FULFILMENT", "NAME", "PHONE", "ADDRESS", "CONFIRMATION"

    private LocalDateTime updatedAt = LocalDateTime.now();

    public ChatConversationContext() {
    }

    public ChatConversationContext(String sessionId) {
        this.sessionId = sessionId;
        this.updatedAt = LocalDateTime.now();
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public void resetOrderDraft() {
        this.draftPharmacyId = null;
        this.draftPharmacyName = null;
        this.draftMedicineId = null;
        this.draftMedicineName = null;
        this.draftQuantity = 1;
        this.draftUnitPrice = null;
        this.draftDeliveryFee = BigDecimal.ZERO;
        this.draftFulfilmentType = null;
        this.customerName = null;
        this.customerPhone = null;
        this.deliveryAddress = null;
        this.landmark = null;
        this.awaitingOrderConfirmation = false;
        this.awaitingField = null;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOrderInProgress() {
        return draftMedicineName != null || draftPharmacyId != null || awaitingField != null || awaitingOrderConfirmation;
    }

    public BigDecimal getEstimatedTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (draftUnitPrice != null && draftQuantity != null && draftQuantity > 0) {
            total = draftUnitPrice.multiply(BigDecimal.valueOf(draftQuantity));
        }
        if ("DELIVERY".equalsIgnoreCase(draftFulfilmentType) && draftDeliveryFee != null) {
            total = total.add(draftDeliveryFee);
        }
        return total;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAuthenticatedUserId() {
        return userId;
    }

    public void setAuthenticatedUserId(Long userId) {
        this.userId = userId;
    }

    public String getLastSymptom() {
        return lastSymptom;
    }

    public void setLastSymptom(String lastSymptom) {
        this.lastSymptom = lastSymptom;
    }

    public String getActiveSymptom() {
        return lastSymptom;
    }

    public void setActiveSymptom(String activeSymptom) {
        this.lastSymptom = activeSymptom;
    }

    public String getLocality() {
        return lastLocality;
    }

    public void setLocality(String locality) {
        this.lastLocality = locality;
    }

    public Double getUserLat() {
        return lastLat;
    }

    public void setUserLat(Double lat) {
        this.lastLat = lat;
    }

    public Double getUserLng() {
        return lastLng;
    }

    public void setUserLng(Double lng) {
        this.lastLng = lng;
    }

    public String getLastDiscussedMedicine() {
        return lastDiscussedMedicine;
    }

    public void setLastDiscussedMedicine(String lastDiscussedMedicine) {
        this.lastDiscussedMedicine = lastDiscussedMedicine;
    }

    public Long getLastDiscussedMedicineId() {
        return lastDiscussedMedicineId;
    }

    public void setLastDiscussedMedicineId(Long lastDiscussedMedicineId) {
        this.lastDiscussedMedicineId = lastDiscussedMedicineId;
    }

    public String getLastLocality() {
        return lastLocality;
    }

    public void setLastLocality(String lastLocality) {
        this.lastLocality = lastLocality;
    }

    public Double getLastLat() {
        return lastLat;
    }

    public void setLastLat(Double lastLat) {
        this.lastLat = lastLat;
    }

    public Double getLastLng() {
        return lastLng;
    }

    public void setLastLng(Double lastLng) {
        this.lastLng = lastLng;
    }

    public Long getDraftPharmacyId() {
        return draftPharmacyId;
    }

    public void setDraftPharmacyId(Long draftPharmacyId) {
        this.draftPharmacyId = draftPharmacyId;
    }

    public String getDraftPharmacyName() {
        return draftPharmacyName;
    }

    public void setDraftPharmacyName(String draftPharmacyName) {
        this.draftPharmacyName = draftPharmacyName;
    }

    public Long getDraftMedicineId() {
        return draftMedicineId;
    }

    public void setDraftMedicineId(Long draftMedicineId) {
        this.draftMedicineId = draftMedicineId;
    }

    public String getDraftMedicineName() {
        return draftMedicineName;
    }

    public void setDraftMedicineName(String draftMedicineName) {
        this.draftMedicineName = draftMedicineName;
    }

    public Integer getDraftQuantity() {
        return draftQuantity != null ? draftQuantity : 1;
    }

    public void setDraftQuantity(Integer draftQuantity) {
        this.draftQuantity = draftQuantity;
    }

    public BigDecimal getDraftUnitPrice() {
        return draftUnitPrice;
    }

    public void setDraftUnitPrice(BigDecimal draftUnitPrice) {
        this.draftUnitPrice = draftUnitPrice;
    }

    public BigDecimal getDraftDeliveryFee() {
        return draftDeliveryFee != null ? draftDeliveryFee : BigDecimal.ZERO;
    }

    public void setDraftDeliveryFee(BigDecimal draftDeliveryFee) {
        this.draftDeliveryFee = draftDeliveryFee;
    }

    public String getDraftFulfilmentType() {
        return draftFulfilmentType;
    }

    public void setDraftFulfilmentType(String draftFulfilmentType) {
        this.draftFulfilmentType = draftFulfilmentType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public boolean isAwaitingOrderConfirmation() {
        return awaitingOrderConfirmation;
    }

    public void setAwaitingOrderConfirmation(boolean awaitingOrderConfirmation) {
        this.awaitingOrderConfirmation = awaitingOrderConfirmation;
    }

    public String getAwaitingField() {
        return awaitingField;
    }

    public void setAwaitingField(String awaitingField) {
        this.awaitingField = awaitingField;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
