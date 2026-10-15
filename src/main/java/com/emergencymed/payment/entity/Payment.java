package com.emergencymed.payment.entity;

import com.emergencymed.payment.enums.PaymentMethod;
import com.emergencymed.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_reference", nullable = false, unique = true, length = 32)
    private String paymentReference;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "pharmacy_id", nullable = false)
    private Long pharmacyId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 5)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.CREATED;

    @Column(name = "gateway_provider", nullable = false, length = 30)
    private String gatewayProvider = "DEMO";

    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "cash_collected_by_user_id")
    private Long cashCollectedByUserId;

    @Column(name = "cash_collected_at")
    private LocalDateTime cashCollectedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Payment() {
    }

    public Payment(String paymentReference, Long orderId, Long userId, Long pharmacyId,
                   BigDecimal amount, String currency, PaymentMethod paymentMethod,
                   PaymentStatus paymentStatus, String gatewayProvider, String idempotencyKey) {
        this.paymentReference = paymentReference;
        this.orderId = orderId;
        this.userId = userId;
        this.pharmacyId = pharmacyId;
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.CREATED;
        this.gatewayProvider = gatewayProvider != null ? gatewayProvider : "DEMO";
        this.idempotencyKey = idempotencyKey;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getGatewayProvider() {
        return gatewayProvider;
    }

    public void setGatewayProvider(String gatewayProvider) {
        this.gatewayProvider = gatewayProvider;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Long getCashCollectedByUserId() {
        return cashCollectedByUserId;
    }

    public void setCashCollectedByUserId(Long cashCollectedByUserId) {
        this.cashCollectedByUserId = cashCollectedByUserId;
    }

    public LocalDateTime getCashCollectedAt() {
        return cashCollectedAt;
    }

    public void setCashCollectedAt(LocalDateTime cashCollectedAt) {
        this.cashCollectedAt = cashCollectedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
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
}
