package com.emergencymed.request.controller;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.enums.Role;
import com.emergencymed.request.dto.*;
import com.emergencymed.request.entity.DeliveryAvailability;
import com.emergencymed.request.entity.EmergencyRequest;
import com.emergencymed.request.exception.AccessForbiddenException;
import com.emergencymed.request.service.DeliveryAssignmentService;
import com.emergencymed.request.service.RequestService;
import com.emergencymed.request.service.impl.DeliveryAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/delivery")
public class DeliveryController {

    private final DeliveryAvailabilityService availabilityService;
    private final DeliveryAssignmentService assignmentService;
    private final RequestService requestService;
    private final CurrentUserProvider currentUserProvider;

    public DeliveryController(
            DeliveryAvailabilityService availabilityService,
            DeliveryAssignmentService assignmentService,
            RequestService requestService,
            CurrentUserProvider currentUserProvider) {
        this.availabilityService = availabilityService;
        this.assignmentService = assignmentService;
        this.requestService = requestService;
        this.currentUserProvider = currentUserProvider;
    }

    private void checkDeliveryRole() {
        if (currentUserProvider.role() != Role.DELIVERY && currentUserProvider.role() != Role.ADMIN) {
            throw new AccessForbiddenException("Only delivery partners have access to delivery operations");
        }
    }

    @GetMapping("/me/availability")
    public ResponseEntity<ApiResponse<DeliveryAvailability>> getMyAvailability() {
        checkDeliveryRole();
        DeliveryAvailability record = availabilityService.getMyAvailability();
        return ResponseEntity.ok(ApiResponse.ok(record));
    }

    @PutMapping("/me/availability")
    public ResponseEntity<ApiResponse<DeliveryAvailability>> updateMyAvailability(@RequestBody UpdateAvailabilityDto dto) {
        checkDeliveryRole();
        DeliveryAvailability updated = availabilityService.updateMyAvailability(dto.isAvailable(), dto.getLat(), dto.getLng());
        return ResponseEntity.ok(ApiResponse.ok("Availability updated successfully", updated));
    }

    @PutMapping("/me/location")
    public ResponseEntity<ApiResponse<DeliveryAvailability>> updateMyLocation(@Valid @RequestBody UpdateLocationDto dto) {
        checkDeliveryRole();
        DeliveryAvailability updated = availabilityService.updateMyLocation(dto.getLat(), dto.getLng());
        return ResponseEntity.ok(ApiResponse.ok("Location updated successfully", updated));
    }

    @GetMapping("/offers")
    public ResponseEntity<ApiResponse<List<OfferResponse>>> getOpenOffers() {
        checkDeliveryRole();
        List<OfferResponse> offers = assignmentService.getOpenOffersForPartner(currentUserProvider.userId());
        return ResponseEntity.ok(ApiResponse.ok(offers));
    }

    @PostMapping("/offers/{assignmentId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptOffer(@PathVariable("assignmentId") Long assignmentId) {
        checkDeliveryRole();
        assignmentService.acceptOffer(assignmentId, currentUserProvider.userId());
        return ResponseEntity.ok(ApiResponse.ok("Delivery offer accepted successfully", null));
    }

    @PostMapping("/offers/{assignmentId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectOffer(
            @PathVariable("assignmentId") Long assignmentId,
            @RequestBody(required = false) Map<String, String> body) {
        checkDeliveryRole();
        String reason = (body != null) ? body.get("reason") : "Partner declined offer";
        assignmentService.rejectOffer(assignmentId, currentUserProvider.userId(), reason);
        return ResponseEntity.ok(ApiResponse.ok("Delivery offer rejected", null));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<PageResponse<RequestDetailResponse>>> getJobs(
            @RequestParam(name = "type", defaultValue = "active") String type,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        checkDeliveryRole();
        size = Math.min(50, Math.max(1, size));
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<EmergencyRequest> jobsPage = assignmentService.getPartnerJobs(currentUserProvider.userId(), type, pageable);
        Page<RequestDetailResponse> mapped = jobsPage.map(r -> requestService.getRequestDetails(r.getId()));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(mapped)));
    }

    @PostMapping("/jobs/{requestId}/picked-up")
    public ResponseEntity<ApiResponse<Void>> markPickedUp(@PathVariable("requestId") Long requestId) {
        checkDeliveryRole();
        assignmentService.markPickedUp(requestId, currentUserProvider.userId());
        return ResponseEntity.ok(ApiResponse.ok("Order marked as picked up from pharmacy", null));
    }

    @PostMapping("/jobs/{requestId}/delivered")
    public ResponseEntity<ApiResponse<Void>> markDelivered(
            @PathVariable("requestId") Long requestId,
            @Valid @RequestBody HandoverDto dto) {
        checkDeliveryRole();
        assignmentService.markDelivered(requestId, currentUserProvider.userId(), dto.getHandoverCode());
        return ResponseEntity.ok(ApiResponse.ok("Order delivery confirmed successfully", null));
    }
}
