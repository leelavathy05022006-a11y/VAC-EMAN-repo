package com.emergencymed.inventory.exception;

import org.springframework.http.HttpStatus;

public class InvalidStateTransitionException extends ApiException {
    private static final long serialVersionUID = 1L;

    public InvalidStateTransitionException(String message) {
        super(HttpStatus.CONFLICT, "INVALID_STATE_TRANSITION", message);
    }
}
