package com.emergencymed.inventory.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends ApiException {
    private static final long serialVersionUID = 1L;

    public InsufficientStockException(String message) {
        super(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", message);
    }
}
