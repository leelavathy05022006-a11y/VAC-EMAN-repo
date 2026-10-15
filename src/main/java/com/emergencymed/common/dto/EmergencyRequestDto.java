package com.emergencymed.common.dto;

public class EmergencyRequestDto {
    private Long id;
    private String referenceCode;
    private Long requesterUserId;
    private Long pharmacyId;
    private String pharmacyName;
    private Long medicineId;
    private String medicineName;
    private Integer quantity;
    private String status; // PENDING, CONFIRMED, DISPATCHED, READY_FOR_PICKUP, DELIVERED, CANCELLED
    private String paymentMode; // CASH_ON_DELIVERY, PAY_AT_PHARMACY
    private String deliveryPartnerName;
    private String handoverOtp;
    private String createdAt;
    private String updatedAt;

    public EmergencyRequestDto() {
    }

    public EmergencyRequestDto(Long id, String referenceCode, Long requesterUserId, Long pharmacyId,
                               String pharmacyName, Long medicineId, String medicineName, Integer quantity,
                               String status, String paymentMode, String deliveryPartnerName,
                               String handoverOtp, String createdAt, String updatedAt) {
        this.id = id;
        this.referenceCode = referenceCode;
        this.requesterUserId = requesterUserId;
        this.pharmacyId = pharmacyId;
        this.pharmacyName = pharmacyName;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.status = status;
        this.paymentMode = paymentMode;
        this.deliveryPartnerName = deliveryPartnerName;
        this.handoverOtp = handoverOtp;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public Long getRequesterUserId() {
        return requesterUserId;
    }

    public void setRequesterUserId(Long requesterUserId) {
        this.requesterUserId = requesterUserId;
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

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getHandoverOtp() {
        return handoverOtp;
    }

    public void setHandoverOtp(String handoverOtp) {
        this.handoverOtp = handoverOtp;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
