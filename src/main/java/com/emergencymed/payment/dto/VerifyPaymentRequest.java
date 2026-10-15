package com.emergencymed.payment.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyPaymentRequest {

    @NotBlank(message = "Payment reference is required")
    private String paymentReference;

    private String gatewayTransactionId;
    private String signature;
    private String simulatedOutcome; // For demo/test mode: "SUCCESS", "FAIL", "PENDING"

    public VerifyPaymentRequest() {
    }

    public VerifyPaymentRequest(String paymentReference, String gatewayTransactionId, String signature) {
        this.paymentReference = paymentReference;
        this.gatewayTransactionId = gatewayTransactionId;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSimulatedOutcome() {
        return simulatedOutcome;
    }

    public void setSimulatedOutcome(String simulatedOutcome) {
        this.simulatedOutcome = simulatedOutcome;
    }
}
