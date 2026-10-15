package com.emergencymed.request.exception;

import org.springframework.http.HttpStatus;

public class NoDeliveryPartnerException extends BaseApiException {
    public NoDeliveryPartnerException(String message) {
        super(HttpStatus.CONFLICT, "NO_DELIVERY_PARTNER", message);
    }
}
