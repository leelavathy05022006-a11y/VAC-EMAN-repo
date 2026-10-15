package com.emergencymed.inventory.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ApiException {
    private static final long serialVersionUID = 1L;

    public DuplicateResourceException(String message) {
        super(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", message);
    }
}
