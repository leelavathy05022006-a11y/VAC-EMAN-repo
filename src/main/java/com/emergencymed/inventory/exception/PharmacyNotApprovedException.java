package com.emergencymed.inventory.exception;

import org.springframework.http.HttpStatus;

public class PharmacyNotApprovedException extends ApiException {
    private static final long serialVersionUID = 1L;

    public PharmacyNotApprovedException(String message) {
        super(HttpStatus.FORBIDDEN, "PHARMACY_NOT_APPROVED", message);
    }
}
