package com.emergencymed.payment.provider;

import com.emergencymed.payment.entity.Payment;
import com.emergencymed.payment.enums.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Explicitly labelled DEMO / TEST Payment Gateway Provider.
 * Simulates UPI Intent, UPI QR, and Card payment initiation and verification
 * for testing without live merchant credentials.
 */
@Component
public class DemoPaymentGatewayProvider implements PaymentGatewayProvider {

    private static final Logger log = LoggerFactory.getLogger(DemoPaymentGatewayProvider.class);
    public static final String PROVIDER_NAME = "DEMO_GATEWAY";

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isDemoMode() {
        return true;
    }

    @Override
    public GatewayInitiationResult initiatePayment(Payment payment) {
        String txId = "DEMO_TX_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        log.info("[DEMO MODE] Initiating payment for ref={}, amount={} {}, method={}",
                payment.getPaymentReference(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod());

        String upiIntent = null;
        String qrData = null;

        if (payment.getPaymentMethod() == PaymentMethod.UPI || payment.getPaymentMethod() == PaymentMethod.UPI_QR) {
            String note = URLEncoder.encode("Emergency Med Order #" + payment.getOrderId(), StandardCharsets.UTF_8);
            upiIntent = String.format("upi://pay?pa=demo@emergencymed&pn=EmergencyMedDemo&am=%s&cu=%s&tr=%s&tn=%s",
                    payment.getAmount().toPlainString(), payment.getCurrency(), payment.getPaymentReference(), note);
            qrData = upiIntent;
        }

        Map<String, String> meta = Map.of(
                "environment", "DEMO_TEST_SANDBOX",
                "notice", "This is an explicitly labelled DEMO/TEST environment. No real funds are moved."
        );

        return new GatewayInitiationResult(txId, qrData, upiIntent, null, meta);
    }

    @Override
    public GatewayVerificationResult verifyPayment(Payment payment, String gatewayTransactionId, String signature, String simulatedOutcome) {
        log.info("[DEMO MODE] Verifying payment ref={}, txId={}, simulatedOutcome={}",
                payment.getPaymentReference(), gatewayTransactionId, simulatedOutcome);

        if ("FAIL".equalsIgnoreCase(simulatedOutcome)) {
            return GatewayVerificationResult.failed(gatewayTransactionId, "Demo user simulated failure / bank declined");
        } else if ("PENDING".equalsIgnoreCase(simulatedOutcome)) {
            return GatewayVerificationResult.pending(gatewayTransactionId);
        }

        // Default in demo mode is verified success
        return GatewayVerificationResult.success(gatewayTransactionId != null ? gatewayTransactionId : "DEMO_TX_CONFIRMED");
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature) {
        // In demo mode, require signature non-blank or starts with "DEMO_SIG"
        return signature != null && !signature.isBlank();
    }
}
