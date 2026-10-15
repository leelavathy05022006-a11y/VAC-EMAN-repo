package com.emergencymed.auth.controller;

import com.emergencymed.auth.dto.*;
import com.emergencymed.auth.service.AuthService;
import com.emergencymed.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/patient")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerPatient(@Valid @RequestBody RegisterPatientRequest req) {
        Long userId = authService.registerPatient(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(Map.of("userId", userId, "role", "PATIENT"), "Patient registered successfully")
        );
    }

    @PostMapping("/register/hospital")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerHospital(@Valid @RequestBody RegisterHospitalRequest req) {
        Long userId = authService.registerHospital(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(Map.of("userId", userId, "role", "HOSPITAL"), "Hospital registered successfully")
        );
    }

    @PostMapping("/register/pharmacy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerPharmacy(@Valid @RequestBody RegisterPharmacyRequest req) {
        Long userId = authService.registerPharmacy(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(Map.of("userId", userId, "role", "PHARMACY", "approvalStatus", "PENDING"),
                        "Pharmacy registration submitted. Awaiting administrator approval.")
        );
    }

    @PostMapping("/register/delivery-partner")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerDeliveryPartner(@Valid @RequestBody RegisterDeliveryRequest req) {
        Long userId = authService.registerDeliveryPartner(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(Map.of("userId", userId, "role", "DELIVERY", "verified", false),
                        "Delivery partner registered successfully")
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(req, request, response);
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse>> getCurrentUser() {
        LoginResponse summary = authService.getCurrentUserSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Session active"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(req);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<Map<String, String>>> getCsrf(CsrfToken csrfToken) {
        if (csrfToken != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    Map.of("token", csrfToken.getToken(), "headerName", csrfToken.getHeaderName()),
                    "CSRF token obtained"
            ));
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of(), "CSRF initialized"));
    }
}
