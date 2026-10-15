package com.emergencymed.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation error occurred"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication is required or session has expired"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),
    PHARMACY_NOT_APPROVED(HttpStatus.FORBIDDEN, "Pharmacy account has not been approved yet"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Requested resource not found"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "Resource already exists"),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "Requested quantity exceeds available stock"),
    INVALID_STATE_TRANSITION(HttpStatus.CONFLICT, "Invalid status transition"),
    NO_DELIVERY_PARTNER(HttpStatus.CONFLICT, "No delivery partner available"),
    PRESCRIPTION_REQUIRED(HttpStatus.UNPROCESSABLE_ENTITY, "Prescription required for this item"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
