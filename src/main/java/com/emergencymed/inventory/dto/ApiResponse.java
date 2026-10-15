package com.emergencymed.inventory.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        List<FieldErrorDto> errors,
        String timestamp
) {
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now().toString());
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, "Operation successful");
    }

    public static <T> ApiResponse<T> error(String message, List<FieldErrorDto> errors) {
        return new ApiResponse<>(false, message, null, errors, LocalDateTime.now().toString());
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(message, null);
    }
}
