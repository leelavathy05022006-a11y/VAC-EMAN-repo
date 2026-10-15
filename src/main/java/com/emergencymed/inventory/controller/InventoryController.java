package com.emergencymed.inventory.controller;

import com.emergencymed.inventory.dto.*;
import com.emergencymed.inventory.service.InventoryManagementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/inventory", "/api/inventory"})
public class InventoryController {
    private final InventoryManagementService inventoryService;

    public InventoryController(InventoryManagementService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ApiResponse<PageResponse<InventoryRowResponse>> getInventory(
            @RequestParam(required = false) Long pharmacyId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {
        int max = Math.min(Math.max(size, 1), 50);
        PageResponse<InventoryRowResponse> result = PageResponse.from(
                inventoryService.getInventory(pharmacyId, q, status, PageRequest.of(page, max, Sort.by(sort)))
        );
        return ApiResponse.ok(result);
    }

    @GetMapping("/summary")
    public ApiResponse<InventorySummaryDto> getSummary(@RequestParam(required = false) Long pharmacyId) {
        return ApiResponse.ok(inventoryService.getSummary(pharmacyId));
    }

    @GetMapping("/alerts")
    public ApiResponse<List<InventoryRowResponse>> getAlerts(@RequestParam(required = false) Long pharmacyId) {
        return ApiResponse.ok(inventoryService.getAlerts(pharmacyId));
    }

    @GetMapping("/{id}")
    public ApiResponse<InventoryRowResponse> getInventoryById(@PathVariable Long id) {
        return ApiResponse.ok(inventoryService.getInventoryById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InventoryRowResponse> addStock(@Valid @RequestBody AddInventoryRequest req) {
        return ApiResponse.ok(inventoryService.addStock(req), "Stock added successfully");
    }

    @PutMapping("/{id}")
    public ApiResponse<InventoryRowResponse> updateInventory(@PathVariable Long id, @Valid @RequestBody UpdateInventoryRequest req) {
        return ApiResponse.ok(inventoryService.updateInventory(id, req), "Inventory updated successfully");
    }

    @GetMapping("/{id}/history")
    public ApiResponse<PageResponse<InventoryTransactionDto>> getHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int max = Math.min(Math.max(size, 1), 50);
        PageResponse<InventoryTransactionDto> result = PageResponse.from(
                inventoryService.getTransactions(id, PageRequest.of(page, max))
        );
        return ApiResponse.ok(result);
    }

    @PostMapping("/{id}/stock-in")
    public ApiResponse<InventoryRowResponse> stockIn(@PathVariable Long id, @Valid @RequestBody StockInRequest req) {
        return ApiResponse.ok(inventoryService.stockIn(id, req), "Stock-in successful");
    }

    @PostMapping("/{id}/adjust")
    public ApiResponse<InventoryRowResponse> adjustStock(@PathVariable Long id, @Valid @RequestBody AdjustStockRequest req) {
        return ApiResponse.ok(inventoryService.adjustStock(id, req), "Stock adjustment successful");
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<InventoryRowResponse> confirmSampleRow(@PathVariable Long id) {
        return ApiResponse.ok(inventoryService.confirmSampleRow(id), "Row confirmed to Live");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> removeInventory(@PathVariable Long id) {
        inventoryService.removeInventory(id);
        return ApiResponse.ok(null, "Inventory row deleted successfully");
    }

    @GetMapping("/{id}/transactions")
    public ApiResponse<PageResponse<InventoryTransactionDto>> getTransactions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int max = Math.min(Math.max(size, 1), 50);
        PageResponse<InventoryTransactionDto> result = PageResponse.from(
                inventoryService.getTransactions(id, PageRequest.of(page, max))
        );
        return ApiResponse.ok(result);
    }
}
