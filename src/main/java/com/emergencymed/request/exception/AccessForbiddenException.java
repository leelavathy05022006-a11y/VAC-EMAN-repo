package com.emergencymed.request.exception;

import org.springframework.http.HttpStatus;

public class AccessForbiddenException extends BaseApiException {
    public AccessForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }
}
