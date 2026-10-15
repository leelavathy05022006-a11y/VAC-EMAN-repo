package com.emergencymed.payment.dto;

import java.math.BigDecimal;

public class PaymentCallbackRequest {

    private String paymentReference;
    private String gatewayTransactionId;
    private String status; // "PAID", "FAILED", "PENDING"
    private BigDecimal amount;
    private String currency;
    private String signature;

    public PaymentCallbackRequest() {
    }

    public PaymentCallbackRequest(String paymentReference, String gatewayTransactionId, String status, BigDecimal amount, String currency, String signature) {
        this.paymentReference = paymentReference;
        this.gatewayTransactionId = gatewayTransactionId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.signature = signature;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
