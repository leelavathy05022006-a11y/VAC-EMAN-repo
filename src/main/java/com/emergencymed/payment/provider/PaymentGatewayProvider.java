package com.emergencymed.payment.provider;

import com.emergencymed.payment.entity.Payment;

public interface PaymentGatewayProvider {

    String getProviderName();

    boolean isDemoMode();

    GatewayInitiationResult initiatePayment(Payment payment);

    GatewayVerificationResult verifyPayment(Payment payment, String gatewayTransactionId, String signature, String simulatedOutcome);

    boolean validateWebhookSignature(String payload, String signature);
}
