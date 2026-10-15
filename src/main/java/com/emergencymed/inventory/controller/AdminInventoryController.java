package com.emergencymed.inventory.controller;

import com.emergencymed.inventory.dto.ApiResponse;
import com.emergencymed.inventory.dto.InventoryRowResponse;
import com.emergencymed.inventory.dto.PageResponse;
import com.emergencymed.inventory.service.InventoryManagementService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/admin/inventory", "/api/admin/inventory"})
public class AdminInventoryController {
    private final InventoryManagementService inventoryService;

    public AdminInventoryController(InventoryManagementService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ApiResponse<PageResponse<InventoryRowResponse>> getAdminInventory(
            @RequestParam Long pharmacyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int max = Math.min(Math.max(size, 1), 50);
        PageResponse<InventoryRowResponse> result = PageResponse.from(
                inventoryService.getAdminInventoryByPharmacy(pharmacyId, PageRequest.of(page, max))
        );
        return ApiResponse.ok(result);
    }
}
