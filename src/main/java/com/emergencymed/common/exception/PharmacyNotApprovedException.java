package com.emergencymed.common.exception;

import org.springframework.http.HttpStatus;

public class PharmacyNotApprovedException extends ApiException {
    public PharmacyNotApprovedException(String message) {
        super(HttpStatus.FORBIDDEN, "PHARMACY_NOT_APPROVED", message);
    }
}
