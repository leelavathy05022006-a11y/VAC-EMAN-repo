package com.emergencymed.request.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BaseApiException {
    public InsufficientStockException(String message) {
        super(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", message);
    }
}
