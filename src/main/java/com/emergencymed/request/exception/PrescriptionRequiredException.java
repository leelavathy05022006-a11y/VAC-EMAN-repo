package com.emergencymed.request.exception;

import org.springframework.http.HttpStatus;

public class PrescriptionRequiredException extends BaseApiException {
    public PrescriptionRequiredException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "PRESCRIPTION_REQUIRED", message);
    }
}
