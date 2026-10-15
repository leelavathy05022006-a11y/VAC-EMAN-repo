package com.emergencymed.request.controller;

import com.emergencymed.request.dto.*;
import com.emergencymed.request.service.RequestService;
import com.emergencymed.request.service.RequestSummaryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/requests")
public class EmergencyRequestController {

    private final RequestService requestService;
    private final RequestSummaryService summaryService;

    public EmergencyRequestController(RequestService requestService, RequestSummaryService summaryService) {
        this.requestService = requestService;
        this.summaryService = summaryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RequestDetailResponse>> createRequest(@Valid @RequestBody CreateRequestDto dto) {
        RequestDetailResponse response = requestService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Emergency medicine request created successfully", response));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<PageResponse<RequestDetailResponse>>> getMyRequests(
            @RequestParam(name = "tab", defaultValue = "active") String tab,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        size = Math.min(50, Math.max(1, size));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RequestDetailResponse> results = requestService.getMyRequests(tab, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(results)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RequestDetailResponse>> getRequestDetails(@PathVariable("id") Long id) {
        RequestDetailResponse details = requestService.getRequestDetails(id);
        return ResponseEntity.ok(ApiResponse.ok(details));
    }

    @GetMapping("/{id}/live-tracking")
    public ResponseEntity<ApiResponse<LiveTrackingResponse>> getLiveTracking(@PathVariable("id") Long id) {
        LiveTrackingResponse tracking = requestService.getLiveTracking(id);
        return ResponseEntity.ok(ApiResponse.ok(tracking));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<RequestSummaryResponse>> getSummary() {
        RequestSummaryResponse summary = summaryService.getSummary();
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<RequestDetailResponse>> cancelRequest(
            @PathVariable("id") Long id,
            @Valid @RequestBody CancelRequestDto dto) {
        RequestDetailResponse response = requestService.cancelRequest(id, dto.getReason());
        return ResponseEntity.ok(ApiResponse.ok("Request cancelled successfully", response));
    }

    @PostMapping("/{id}/switch-to-pickup")
    public ResponseEntity<ApiResponse<RequestDetailResponse>> switchToPickup(@PathVariable("id") Long id) {
        RequestDetailResponse response = requestService.switchToPickup(id);
        return ResponseEntity.ok(ApiResponse.ok("Request switched to self-pickup successfully", response));
    }

    @GetMapping("/pharmacy")
    public ResponseEntity<ApiResponse<PageResponse<RequestDetailResponse>>> getPharmacyRequests(
            @RequestParam(name = "tab", defaultValue = "new") String tab,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        size = Math.min(50, Math.max(1, size));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RequestDetailResponse> results = requestService.getPharmacyRequests(tab, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(results)));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<RequestDetailResponse>> acceptRequest(@PathVariable("id") Long id) {
        RequestDetailResponse response = requestService.acceptRequest(id);
        return ResponseEntity.ok(ApiResponse.ok("Request accepted successfully", response));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<RequestDetailResponse>> rejectRequest(
            @PathVariable("id") Long id,
            @Valid @RequestBody RejectRequestDto dto) {
        RequestDetailResponse response = requestService.rejectRequest(id, dto.getReason());
        return ResponseEntity.ok(ApiResponse.ok("Request rejected successfully", response));
    }

    @PostMapping("/{id}/retry-delivery")
    public ResponseEntity<ApiResponse<RequestDetailResponse>> retryDelivery(@PathVariable("id") Long id) {
        RequestDetailResponse response = requestService.retryDelivery(id);
        return ResponseEntity.ok(ApiResponse.ok("Delivery partner assignment retried", response));
    }

    @PostMapping("/{id}/handover")
    public ResponseEntity<ApiResponse<RequestDetailResponse>> handoverOrder(@PathVariable("id") Long id) {
        RequestDetailResponse response = requestService.handoverOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Order handover completed successfully", response));
    }
}
