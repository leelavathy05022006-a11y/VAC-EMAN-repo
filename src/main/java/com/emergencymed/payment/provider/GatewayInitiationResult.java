package com.emergencymed.payment.provider;

import java.util.Map;

public record GatewayInitiationResult(
        String gatewayTransactionId,
        String qrCodeData,
        String upiIntentUrl,
        String redirectUrl,
        Map<String, String> providerMetadata
) {
    public static GatewayInitiationResult of(String gatewayTransactionId, String qrCodeData, String upiIntentUrl) {
        return new GatewayInitiationResult(gatewayTransactionId, qrCodeData, upiIntentUrl, null, Map.of());
    }
}
