package com.emergencymed.request.exception;

import org.springframework.http.HttpStatus;

public class InvalidStateTransitionException extends BaseApiException {
    public InvalidStateTransitionException(String message) {
        super(HttpStatus.CONFLICT, "INVALID_STATE_TRANSITION", message);
    }
}
