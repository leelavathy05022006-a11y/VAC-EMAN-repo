package com.emergencymed.request.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emergency_requests", indexes = {
        @Index(name = "idx_er_requester_status", columnList = "requester_user_id, status"),
        @Index(name = "idx_er_pharmacy_status", columnList = "pharmacy_id, status"),
        @Index(name = "idx_er_status_expires", columnList = "status, expires_at")
})
public class EmergencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_code", nullable = false, unique = true, length = 20)
    private String referenceCode;

    @Column(name = "requester_user_id", nullable = false)
    private Long requesterUserId;

    @Column(name = "requester_type", nullable = false, length = 10)
    private String requesterType; // PATIENT or HOSPITAL

    @Column(name = "pharmacy_id", nullable = false)
    private Long pharmacyId;

    @Column(name = "fulfilment_type", nullable = false, length = 20)
    private String fulfilmentType; // DELIVERY or SELF_PICKUP

    @Column(nullable = false, length = 10)
    private String priority; // CRITICAL or URGENT

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "patient_name", nullable = false, length = 100)
    private String patientName;

    @Column(name = "patient_phone", nullable = false, length = 15)
    private String patientPhone;

    @Column(name = "doctor_name", length = 100)
    private String doctorName;

    @Column(name = "doctor_reg_no", length = 40)
    private String doctorRegNo;

    @Column(name = "delivery_address", length = 255)
    private String deliveryAddress;

    @Column(name = "delivery_lat", precision = 9, scale = 6)
    private BigDecimal deliveryLat;

    @Column(name = "delivery_lng", precision = 9, scale = 6)
    private BigDecimal deliveryLng;

    @Column(name = "delivery_partner_user_id")
    private Long deliveryPartnerUserId;

    @Column(name = "items_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemsTotal;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "grand_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal grandTotal;

    @Column(name = "payment_mode", nullable = false, length = 20)
    private String paymentMode; // PAY_AT_PHARMACY or CASH_ON_DELIVERY

    @Column(name = "prescription_status", nullable = false, length = 15)
    private String prescriptionStatus; // NONE, PENDING, VERIFIED, REJECTED

    @Column(name = "handover_code", columnDefinition = "CHAR(4)", length = 4)
    private String handoverCode;

    @Column(name = "delivery_attempts", nullable = false)
    private int deliveryAttempts = 0;

    @Column(name = "stock_committed", nullable = false)
    private boolean stockCommitted = false;

    @Column(length = 500)
    private String notes;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "cancelled_by_role", length = 10)
    private String cancelledByRole;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RequestItem> items = new ArrayList<>();

    public EmergencyRequest() {
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

    public String getRequesterType() {
        return requesterType;
    }

    public void setRequesterType(String requesterType) {
        this.requesterType = requesterType;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getFulfilmentType() {
        return fulfilmentType;
    }

    public void setFulfilmentType(String fulfilmentType) {
        this.fulfilmentType = fulfilmentType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorRegNo() {
        return doctorRegNo;
    }

    public void setDoctorRegNo(String doctorRegNo) {
        this.doctorRegNo = doctorRegNo;
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

    public Long getDeliveryPartnerUserId() {
        return deliveryPartnerUserId;
    }

    public void setDeliveryPartnerUserId(Long deliveryPartnerUserId) {
        this.deliveryPartnerUserId = deliveryPartnerUserId;
    }

    public BigDecimal getItemsTotal() {
        return itemsTotal;
    }

    public void setItemsTotal(BigDecimal itemsTotal) {
        this.itemsTotal = itemsTotal;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getPrescriptionStatus() {
        return prescriptionStatus;
    }

    public void setPrescriptionStatus(String prescriptionStatus) {
        this.prescriptionStatus = prescriptionStatus;
    }

    public String getHandoverCode() {
        return handoverCode;
    }

    public void setHandoverCode(String handoverCode) {
        this.handoverCode = handoverCode;
    }

    public int getDeliveryAttempts() {
        return deliveryAttempts;
    }

    public void setDeliveryAttempts(int deliveryAttempts) {
        this.deliveryAttempts = deliveryAttempts;
    }

    public boolean isStockCommitted() {
        return stockCommitted;
    }

    public void setStockCommitted(boolean stockCommitted) {
        this.stockCommitted = stockCommitted;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getCancelledByRole() {
        return cancelledByRole;
    }

    public void setCancelledByRole(String cancelledByRole) {
        this.cancelledByRole = cancelledByRole;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<RequestItem> getItems() {
        return items;
    }

    public void setItems(List<RequestItem> items) {
        this.items = items;
    }

    public void addItem(RequestItem item) {
        items.add(item);
        item.setRequest(this);
    }
}
