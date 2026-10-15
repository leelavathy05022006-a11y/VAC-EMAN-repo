package com.emergencymed.payment.dto;

import com.emergencymed.payment.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

public class OrderCheckoutSummaryDto {

    private Long orderId;
    private String referenceCode;
    private String orderStatus;
    private Long pharmacyId;
    private String pharmacyName;
    private String fulfilmentType;
    private BigDecimal medicineCost;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private String currency = "INR";
    private boolean codSupported;
    private List<PaymentMethod> supportedPaymentMethods;
    private PaymentResponse existingPayment;
    private List<ItemSummary> items;

    public static class ItemSummary {
        private Long medicineId;
        private String medicineName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public ItemSummary() {
        }

        public ItemSummary(Long medicineId, String medicineName, int quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
            this.medicineId = medicineId;
            this.medicineName = medicineName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
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

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }

    public OrderCheckoutSummaryDto() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
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

    public String getFulfilmentType() {
        return fulfilmentType;
    }

    public void setFulfilmentType(String fulfilmentType) {
        this.fulfilmentType = fulfilmentType;
    }

    public BigDecimal getMedicineCost() {
        return medicineCost;
    }

    public void setMedicineCost(BigDecimal medicineCost) {
        this.medicineCost = medicineCost;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isCodSupported() {
        return codSupported;
    }

    public void setCodSupported(boolean codSupported) {
        this.codSupported = codSupported;
    }

    public List<PaymentMethod> getSupportedPaymentMethods() {
        return supportedPaymentMethods;
    }

    public void setSupportedPaymentMethods(List<PaymentMethod> supportedPaymentMethods) {
        this.supportedPaymentMethods = supportedPaymentMethods;
    }

    public PaymentResponse getExistingPayment() {
        return existingPayment;
    }

    public void setExistingPayment(PaymentResponse existingPayment) {
        this.existingPayment = existingPayment;
    }

    public List<ItemSummary> getItems() {
        return items;
    }

    public void setItems(List<ItemSummary> items) {
        this.items = items;
    }
}
