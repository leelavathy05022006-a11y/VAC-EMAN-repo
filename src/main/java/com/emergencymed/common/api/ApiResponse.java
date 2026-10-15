package com.emergencymed.common.api;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<ErrorItem> errors;
    private String timestamp;

    public ApiResponse() {
        this.timestamp = Instant.now().toString();
        this.errors = Collections.emptyList();
    }

    public ApiResponse(boolean success, String message, T data, List<ErrorItem> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors != null ? errors : Collections.emptyList();
        this.timestamp = Instant.now().toString();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, Collections.emptyList());
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    public static <T> ApiResponse<T> error(String message, List<ErrorItem> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }

    public static <T> ApiResponse<T> error(String message, String field, String errorMessage) {
        return new ApiResponse<>(false, message, null, List.of(new ErrorItem(field, errorMessage)));
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, Collections.emptyList());
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<ErrorItem> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorItem> errors) {
        this.errors = errors;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
