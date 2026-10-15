package com.emergencymed.chatbot.dto;

public class OrderSummaryCardDto {
    private String referenceCode;
    private String status;
    private String medicineName;
    private String pharmacyName;
    private Integer quantity;
    private String paymentMode;
    private String deliveryPartnerName;
    private String createdAt;
    private String handoverOtp;
    private java.math.BigDecimal totalAmount;

    public OrderSummaryCardDto() {
    }

    public OrderSummaryCardDto(String referenceCode, String status, String medicineName, String pharmacyName,
                               Integer quantity, String paymentMode, String deliveryPartnerName, String createdAt) {
        this(referenceCode, status, medicineName, pharmacyName, quantity, paymentMode, deliveryPartnerName, createdAt, null, null);
    }

    public OrderSummaryCardDto(String referenceCode, String status, String medicineName, String pharmacyName,
                               Integer quantity, String paymentMode, String deliveryPartnerName, String createdAt,
                               String handoverOtp, java.math.BigDecimal totalAmount) {
        this.referenceCode = referenceCode;
        this.status = status;
        this.medicineName = medicineName;
        this.pharmacyName = pharmacyName;
        this.quantity = quantity;
        this.paymentMode = paymentMode;
        this.deliveryPartnerName = deliveryPartnerName;
        this.createdAt = createdAt;
        this.handoverOtp = handoverOtp;
        this.totalAmount = totalAmount;
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

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getDeliveryPartnerName() {
        return deliveryPartnerName;
    }

    public void setDeliveryPartnerName(String deliveryPartnerName) {
        this.deliveryPartnerName = deliveryPartnerName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getHandoverOtp() {
        return handoverOtp;
    }

    public void setHandoverOtp(String handoverOtp) {
        this.handoverOtp = handoverOtp;
    }

    public java.math.BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(java.math.BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
