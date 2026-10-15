package com.emergencymed.payment.provider;

import com.emergencymed.payment.enums.PaymentStatus;

public record GatewayVerificationResult(
        boolean success,
        PaymentStatus paymentStatus,
        String gatewayTransactionId,
        String failureReason
) {
    public static GatewayVerificationResult success(String gatewayTransactionId) {
        return new GatewayVerificationResult(true, PaymentStatus.PAID, gatewayTransactionId, null);
    }

    public static GatewayVerificationResult failed(String gatewayTransactionId, String reason) {
        return new GatewayVerificationResult(false, PaymentStatus.FAILED, gatewayTransactionId, reason);
    }

    public static GatewayVerificationResult pending(String gatewayTransactionId) {
        return new GatewayVerificationResult(false, PaymentStatus.PENDING, gatewayTransactionId, "Payment is still processing");
    }
}
