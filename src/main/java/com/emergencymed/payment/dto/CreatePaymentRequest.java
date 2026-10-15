package com.emergencymed.payment.dto;

import com.emergencymed.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public class CreatePaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String idempotencyKey;

    public CreatePaymentRequest() {
    }

    public CreatePaymentRequest(Long orderId, PaymentMethod paymentMethod, String idempotencyKey) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
