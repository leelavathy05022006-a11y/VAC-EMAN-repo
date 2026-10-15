package com.emergencymed.auth.controller;

import com.emergencymed.auth.dto.AdminSummaryDto;
import com.emergencymed.auth.dto.UserStatusRequest;
import com.emergencymed.auth.entity.User;
import com.emergencymed.auth.service.PharmacyApprovalService;
import com.emergencymed.common.api.ApiResponse;
import com.emergencymed.common.api.PageResponse;
import com.emergencymed.common.contract.dto.UserSummaryDto;
import com.emergencymed.common.enums.Role;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final PharmacyApprovalService pharmacyApprovalService;

    public AdminUserController(PharmacyApprovalService pharmacyApprovalService) {
        this.pharmacyApprovalService = pharmacyApprovalService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminSummaryDto>> getSummary() {
        AdminSummaryDto summary = pharmacyApprovalService.getAdminSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "System summary retrieved"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<UserSummaryDto>>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        size = Math.min(Math.max(size, 1), 50);
        Page<User> usersPage = pharmacyApprovalService.getUsers(
                role, active, q, PageRequest.of(page, size, Sort.by("id").descending())
        );

        Page<UserSummaryDto> dtoPage = usersPage.map(u -> new UserSummaryDto(
                u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole(), u.isActive()
        ));

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(dtoPage), "Users retrieved"));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest req
    ) {
        pharmacyApprovalService.updateUserStatus(id, req.active());
        return ResponseEntity.ok(ApiResponse.success(null, "User status updated successfully"));
    }

    @PatchMapping("/delivery-partners/{userId}/verify")
    public ResponseEntity<ApiResponse<Void>> verifyDeliveryPartner(@PathVariable Long userId) {
        pharmacyApprovalService.verifyDeliveryPartner(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Delivery partner verified successfully"));
    }
}
