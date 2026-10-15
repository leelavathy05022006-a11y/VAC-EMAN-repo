package com.emergencymed.request.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<ApiError> errors = new ArrayList<>();
    private String timestamp;

    public ApiResponse() {
        this.timestamp = Instant.now().toString();
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = new ArrayList<>();
        this.timestamp = Instant.now().toString();
    }

    public ApiResponse(boolean success, String message, T data, List<ApiError> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.timestamp = Instant.now().toString();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Request processed successfully", data);
    }

    public static <T> ApiResponse<T> error(String message, List<ApiError> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, new ArrayList<>());
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

    public List<ApiError> getErrors() {
        return errors;
    }

    public void setErrors(List<ApiError> errors) {
        this.errors = errors;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
