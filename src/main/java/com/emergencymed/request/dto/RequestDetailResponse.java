package com.emergencymed.request.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RequestDetailResponse {

    private Long id;
    private String referenceCode;
    private Long requesterUserId;
    private String requesterType;
    private Long pharmacyId;
    private String pharmacyName;
    private String pharmacyAddress;
    private String pharmacyPhone;
    private BigDecimal pharmacyLat;
    private BigDecimal pharmacyLng;

    private String fulfilmentType;
    private String priority;
    private String status;

    private String patientName;
    private String patientPhone;
    private String doctorName;
    private String doctorRegNo;

    private String deliveryAddress;
    private BigDecimal deliveryLat;
    private BigDecimal deliveryLng;

    private Long deliveryPartnerUserId;
    private String deliveryPartnerName;
    private String deliveryPartnerPhone;
    private BigDecimal deliveryPartnerLat;
    private BigDecimal deliveryPartnerLng;
    private LocalDateTime deliveryPartnerLastLocationAt;

    private BigDecimal itemsTotal;
    private BigDecimal deliveryFee;
    private BigDecimal grandTotal;
    private String paymentMode;

    private String prescriptionStatus;
    private Long prescriptionId;
    private String prescriptionOriginalName;

    private String handoverCode;
    private int deliveryAttempts;
    private boolean stockCommitted;

    private String notes;
    private String rejectionReason;
    private String cancelReason;
    private String cancelledByRole;

    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ItemDetail> items = new ArrayList<>();
    private List<TimelineItem> history = new ArrayList<>();

    private boolean canCancel;
    private boolean canSwitchToPickup;
    private boolean canAccept;
    private boolean canReject;
    private boolean canRetryDelivery;
    private boolean canHandover;

    public static class ItemDetail {
        private Long id;
        private Long medicineId;
        private Long inventoryId;
        private String medicineName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
        private boolean requiresPrescription;
        private Integer currentStock;

        public ItemDetail() {
        }

        public ItemDetail(Long id, Long medicineId, Long inventoryId, String medicineName, int quantity, BigDecimal unitPrice, BigDecimal lineTotal, boolean requiresPrescription, Integer currentStock) {
            this.id = id;
            this.medicineId = medicineId;
            this.inventoryId = inventoryId;
            this.medicineName = medicineName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
            this.requiresPrescription = requiresPrescription;
            this.currentStock = currentStock;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getMedicineId() {
            return medicineId;
        }

        public void setMedicineId(Long medicineId) {
            this.medicineId = medicineId;
        }

        public Long getInventoryId() {
            return inventoryId;
        }

        public void setInventoryId(Long inventoryId) {
            this.inventoryId = inventoryId;
        }

        public String getMedicineName() {
            return medicineName;
        }

        public void setMedicineName(String medicineName) {
            this.medicineName = medicineName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getLineTotal() {
            return lineTotal;
        }

        public void setLineTotal(BigDecimal lineTotal) {
            this.lineTotal = lineTotal;
        }

        public boolean isRequiresPrescription() {
            return requiresPrescription;
        }

        public void setRequiresPrescription(boolean requiresPrescription) {
            this.requiresPrescription = requiresPrescription;
        }

        public Integer getCurrentStock() {
            return currentStock;
        }

        public void setCurrentStock(Integer currentStock) {
            this.currentStock = currentStock;
        }
    }

    public RequestDetailResponse() {
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

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getPrescriptionOriginalName() {
        return prescriptionOriginalName;
    }

    public void setPrescriptionOriginalName(String prescriptionOriginalName) {
        this.prescriptionOriginalName = prescriptionOriginalName;
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

    public List<ItemDetail> getItems() {
        return items;
    }

    public void setItems(List<ItemDetail> items) {
        this.items = items;
    }

    public List<TimelineItem> getHistory() {
        return history;
    }

    public void setHistory(List<TimelineItem> history) {
        this.history = history;
    }

    public boolean isCanCancel() {
        return canCancel;
    }

    public void setCanCancel(boolean canCancel) {
        this.canCancel = canCancel;
    }

    public boolean isCanSwitchToPickup() {
        return canSwitchToPickup;
    }

    public void setCanSwitchToPickup(boolean canSwitchToPickup) {
        this.canSwitchToPickup = canSwitchToPickup;
    }

    public boolean isCanAccept() {
        return canAccept;
    }

    public void setCanAccept(boolean canAccept) {
        this.canAccept = canAccept;
    }

    public boolean isCanReject() {
        return canReject;
    }

    public void setCanReject(boolean canReject) {
        this.canReject = canReject;
    }

    public boolean isCanRetryDelivery() {
        return canRetryDelivery;
    }

    public void setCanRetryDelivery(boolean canRetryDelivery) {
        this.canRetryDelivery = canRetryDelivery;
    }

    public boolean isCanHandover() {
        return canHandover;
    }

    public void setCanHandover(boolean canHandover) {
        this.canHandover = canHandover;
    }

    public BigDecimal getDeliveryPartnerLat() {
        return deliveryPartnerLat;
    }

    public void setDeliveryPartnerLat(BigDecimal deliveryPartnerLat) {
        this.deliveryPartnerLat = deliveryPartnerLat;
    }

    public BigDecimal getDeliveryPartnerLng() {
        return deliveryPartnerLng;
    }

    public void setDeliveryPartnerLng(BigDecimal deliveryPartnerLng) {
        this.deliveryPartnerLng = deliveryPartnerLng;
    }

    public LocalDateTime getDeliveryPartnerLastLocationAt() {
        return deliveryPartnerLastLocationAt;
    }

    public void setDeliveryPartnerLastLocationAt(LocalDateTime deliveryPartnerLastLocationAt) {
        this.deliveryPartnerLastLocationAt = deliveryPartnerLastLocationAt;
    }
}
