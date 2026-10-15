package com.emergencymed.auth.controller;

import com.emergencymed.auth.dto.UpdatePharmacyRequest;
import com.emergencymed.auth.service.PharmacyApprovalService;
import com.emergencymed.common.api.ApiResponse;
import com.emergencymed.common.contract.dto.PharmacySummaryDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pharmacies")
public class PharmacyAccountController {

    private final PharmacyApprovalService pharmacyApprovalService;

    public PharmacyAccountController(PharmacyApprovalService pharmacyApprovalService) {
        this.pharmacyApprovalService = pharmacyApprovalService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PHARMACY')")
    public ResponseEntity<ApiResponse<PharmacySummaryDto>> getOwnPharmacy() {
        PharmacySummaryDto dto = pharmacyApprovalService.getOwnPharmacy();
        return ResponseEntity.ok(ApiResponse.success(dto, "Pharmacy details retrieved"));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PHARMACY')")
    public ResponseEntity<ApiResponse<PharmacySummaryDto>> updateOwnPharmacy(@Valid @RequestBody UpdatePharmacyRequest req) {
        PharmacySummaryDto updated = pharmacyApprovalService.updateOwnPharmacy(req);
        return ResponseEntity.ok(ApiResponse.success(updated, "Pharmacy profile updated successfully"));
    }
}
