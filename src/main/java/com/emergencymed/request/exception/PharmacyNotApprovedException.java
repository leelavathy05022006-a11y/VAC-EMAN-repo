package com.emergencymed.request.exception;

import org.springframework.http.HttpStatus;

public class PharmacyNotApprovedException extends BaseApiException {
    public PharmacyNotApprovedException(String message) {
        super(HttpStatus.FORBIDDEN, "PHARMACY_NOT_APPROVED", message);
    }
}
