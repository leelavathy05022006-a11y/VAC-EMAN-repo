package com.emergencymed.common.api;

public record ErrorItem(String field, String message) {
    public static ErrorItem of(String field, String message) {
        return new ErrorItem(field, message);
    }

    public static ErrorItem of(String message) {
        return new ErrorItem(null, message);
    }
}
