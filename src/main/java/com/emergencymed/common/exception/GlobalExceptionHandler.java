package com.emergencymed.common.exception;

import com.emergencymed.common.api.ApiResponse;
import com.emergencymed.common.api.ErrorItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        List<ErrorItem> errors = ex.getErrors().isEmpty()
                ? List.of(ErrorItem.of(ex.getErrorCode().name(), ex.getMessage()))
                : ex.getErrors();

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage(), errors);
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(com.emergencymed.request.exception.BaseApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseApiException(com.emergencymed.request.exception.BaseApiException ex) {
        log.warn("API Exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        List<ErrorItem> errors = List.of(ErrorItem.of(ex.getErrorCode(), ex.getMessage()));
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.error(ex.getMessage(), errors));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        log.warn("API Exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        List<ErrorItem> errors = List.of(ErrorItem.of(ex.getErrorCode(), ex.getMessage()));
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.error(ex.getMessage(), errors));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ErrorItem> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(ErrorItem.of(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        String summary = errors.isEmpty() ? "Validation failed" : errors.get(0).message();
        ApiResponse<Void> response = ApiResponse.error(summary, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                "Access denied: insufficient permissions to perform this action",
                List.of(ErrorItem.of("FORBIDDEN", ex.getMessage()))
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                "Invalid email or password",
                List.of(ErrorItem.of("UNAUTHORIZED", "Authentication failed"))
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(
                "Malformed JSON request or invalid field format",
                List.of(ErrorItem.of("MALFORMED_REQUEST", "Please check your request body format"))
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        log.warn("Resource not found: {}", ex.getResourcePath());
        ApiResponse<Void> response = ApiResponse.error(
                "Requested resource was not found: " + ex.getResourcePath(),
                List.of(ErrorItem.of("NOT_FOUND", ex.getMessage()))
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                List.of(ErrorItem.of("VALIDATION_ERROR", ex.getMessage()))
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled error: ", ex);
        // Never return stack trace
        ApiResponse<Void> response = ApiResponse.error(
                "An unexpected server error occurred. Please try again later.",
                List.of(ErrorItem.of("INTERNAL_ERROR", "Server encountered an error"))
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
