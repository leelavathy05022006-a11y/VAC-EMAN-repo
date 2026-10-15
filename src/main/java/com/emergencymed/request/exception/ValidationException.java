package com.emergencymed.request.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends BaseApiException {
    public ValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }
}
