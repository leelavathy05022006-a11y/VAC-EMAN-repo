package com.emergencymed.common.exception;

import com.emergencymed.common.api.ErrorItem;
import java.util.Collections;
import java.util.List;

public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final List<ErrorItem> errors;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.errors = Collections.emptyList();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errors = Collections.emptyList();
    }

    public BusinessException(ErrorCode errorCode, String field, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errors = List.of(new ErrorItem(field, message));
    }

    public BusinessException(ErrorCode errorCode, String message, List<ErrorItem> errors) {
        super(message);
        this.errorCode = errorCode;
        this.errors = errors != null ? errors : Collections.emptyList();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<ErrorItem> getErrors() {
        return errors;
    }
}
