package com.emergencymed.auth.controller;

import com.emergencymed.auth.dto.RejectSuspendRequest;
import com.emergencymed.auth.service.PharmacyApprovalService;
import com.emergencymed.common.api.ApiResponse;
import com.emergencymed.common.api.PageResponse;
import com.emergencymed.common.contract.dto.PharmacySummaryDto;
import com.emergencymed.common.enums.ApprovalStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/pharmacies")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPharmacyController {

    private final PharmacyApprovalService pharmacyApprovalService;

    public AdminPharmacyController(PharmacyApprovalService pharmacyApprovalService) {
        this.pharmacyApprovalService = pharmacyApprovalService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PharmacySummaryDto>>> getPharmacies(
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        size = Math.min(Math.max(size, 1), 50);
        Page<PharmacySummaryDto> result = pharmacyApprovalService.getPharmacies(
                status, q, PageRequest.of(page, size, Sort.by("id").descending())
        );
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result), "Pharmacies retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PharmacySummaryDto>> getPharmacyDetails(@PathVariable Long id) {
        PharmacySummaryDto dto = pharmacyApprovalService.getPharmacy(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "Pharmacy details retrieved"));
    }

    @RequestMapping(value = "/{id}/approve", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ApiResponse<PharmacySummaryDto>> approvePharmacy(@PathVariable Long id) {
        PharmacySummaryDto approved = pharmacyApprovalService.approvePharmacy(id);
        return ResponseEntity.ok(ApiResponse.success(approved, "Pharmacy approved successfully"));
    }

    @RequestMapping(value = "/{id}/reject", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ApiResponse<PharmacySummaryDto>> rejectPharmacy(
            @PathVariable Long id,
            @Valid @RequestBody RejectSuspendRequest req
    ) {
        PharmacySummaryDto rejected = pharmacyApprovalService.rejectPharmacy(id, req.reason());
        return ResponseEntity.ok(ApiResponse.success(rejected, "Pharmacy rejected successfully"));
    }

    @RequestMapping(value = "/{id}/suspend", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ApiResponse<PharmacySummaryDto>> suspendPharmacy(
            @PathVariable Long id,
            @Valid @RequestBody RejectSuspendRequest req
    ) {
        PharmacySummaryDto suspended = pharmacyApprovalService.suspendPharmacy(id, req.reason());
        return ResponseEntity.ok(ApiResponse.success(suspended, "Pharmacy suspended successfully"));
    }
}
