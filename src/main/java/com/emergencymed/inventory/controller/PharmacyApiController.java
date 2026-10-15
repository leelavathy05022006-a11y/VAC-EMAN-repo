package com.emergencymed.inventory.controller;

import com.emergencymed.common.contract.DeliveryAvailabilityQuery;
import com.emergencymed.common.enums.DeliveryStatus;
import com.emergencymed.inventory.dto.*;
import com.emergencymed.inventory.service.ExternalPharmacyDirectoryService;
import com.emergencymed.inventory.service.InventoryManagementService;
import com.emergencymed.inventory.service.PharmacyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/pharmacies", "/api/pharmacies"})
public class PharmacyApiController {
    private final PharmacyService pharmacyService;
    private final InventoryManagementService inventoryService;
    private final ExternalPharmacyDirectoryService externalDirectoryService;
    private final DeliveryAvailabilityQuery deliveryQuery;

    public PharmacyApiController(PharmacyService pharmacyService,
                                  InventoryManagementService inventoryService,
                                  ExternalPharmacyDirectoryService externalDirectoryService,
                                  DeliveryAvailabilityQuery deliveryQuery) {
        this.pharmacyService = pharmacyService;
        this.inventoryService = inventoryService;
        this.externalDirectoryService = externalDirectoryService;
        this.deliveryQuery = deliveryQuery;
    }

    @GetMapping
    public ApiResponse<List<PharmacyDto>> getPharmacies(
            @RequestParam(required = false, defaultValue = "Chennai") String city,
            @RequestParam(required = false) String locality) {
        return ApiResponse.ok(pharmacyService.getPharmacies(city, locality));
    }

    /**
     * Hyperlocal Discovery around exact user coordinates with configurable radius, facility type, and filters.
     */
    @GetMapping("/discover")
    public ApiResponse<PharmacyDiscoveryResponse> discoverPharmacies(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "10.0") double radiusKm,
            @RequestParam(required = false) String locality,
            @RequestParam(required = false) String facilityType,
            @RequestParam(required = false, defaultValue = "false") Boolean verifiedStockOnly,
            @RequestParam(required = false, defaultValue = "false") Boolean deliveryOnly,
            @RequestParam(required = false, defaultValue = "false") Boolean open24x7Only) {
        double effLat = latitude != null ? latitude : (lat != null ? lat : 13.0827);
        double effLng = longitude != null ? longitude : (lng != null ? lng : 80.2707);
        return ApiResponse.ok(pharmacyService.discoverNearbyPharmacies(
                effLat, effLng, radiusKm, locality, facilityType, verifiedStockOnly, deliveryOnly, open24x7Only));
    }

    @GetMapping("/nearby")
    public ApiResponse<List<PharmacyDto>> getNearbyPharmacies(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "10.0") double radiusKm,
            @RequestParam(required = false) String locality,
            @RequestParam(required = false) String facilityType,
            @RequestParam(required = false, defaultValue = "false") Boolean verifiedStockOnly,
            @RequestParam(required = false, defaultValue = "false") Boolean deliveryOnly,
            @RequestParam(required = false, defaultValue = "false") Boolean open24x7Only) {
        double effLat = latitude != null ? latitude : (lat != null ? lat : 13.0827);
        double effLng = longitude != null ? longitude : (lng != null ? lng : 80.2707);
        PharmacyDiscoveryResponse resp = pharmacyService.discoverNearbyPharmacies(
                effLat, effLng, radiusKm, locality, facilityType, verifiedStockOnly, deliveryOnly, open24x7Only);
        return ApiResponse.ok(resp.pharmacies());
    }

    /**
     * Search by street, area, landmark, or PIN code with distance calculation and facility filter.
     */
    @GetMapping("/search")
    public ApiResponse<List<PharmacyDto>> searchPharmacies(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "10.0") Double radiusKm,
            @RequestParam(required = false) String facilityType) {
        Double effLat = latitude != null ? latitude : lat;
        Double effLng = longitude != null ? longitude : lng;
        return ApiResponse.ok(pharmacyService.searchHyperlocal(query, effLat, effLng, radiusKm, facilityType));
    }

    /**
     * Locality autocomplete / suggestions as the user types.
     */
    @GetMapping("/geocode-suggestions")
    public ApiResponse<List<GeocodeSuggestionDto>> getGeocodeSuggestions(@RequestParam(required = false) String query) {
        return ApiResponse.ok(externalDirectoryService.searchLocalitySuggestions(query));
    }

    /**
     * Reverse geocode coordinates to detected locality name and address.
     */
    @GetMapping("/reverse-geocode")
    public ApiResponse<Map<String, String>> reverseGeocode(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {
        double effLat = latitude != null ? latitude : (lat != null ? lat : 13.0827);
        double effLng = longitude != null ? longitude : (lng != null ? lng : 80.2707);
        String address = externalDirectoryService.reverseGeocode(effLat, effLng);
        return ApiResponse.ok(Map.of(
                "latitude", String.valueOf(effLat),
                "longitude", String.valueOf(effLng),
                "formattedAddress", address
        ));
    }

    /**
     * List distinct localities in Chennai. Supports /localities and /chennai/zones.
     */
    @GetMapping({"/localities", "/chennai/zones"})
    public ApiResponse<List<String>> getLocalities(@RequestParam(required = false, defaultValue = "Chennai") String city) {
        return ApiResponse.ok(pharmacyService.getDistinctLocalities(city));
    }

    @GetMapping("/{id}")
    public ApiResponse<PharmacyDto> getPharmacyById(
            @PathVariable Long id,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        return ApiResponse.ok(pharmacyService.getPharmacyById(id, latitude, longitude));
    }

    /**
     * Real medicine stock availability for a specific pharmacy with available quantity and expiry checks.
     */
    @GetMapping("/{id}/medicine-availability")
    public ApiResponse<List<MedicineAvailabilityItemDto>> getMedicineAvailability(
            @PathVariable Long id,
            @RequestParam(required = false) String query) {
        return ApiResponse.ok(pharmacyService.getMedicineAvailability(id, query));
    }

    /**
     * Real-time delivery availability status query.
     */
    @GetMapping("/{id}/delivery-status")
    public ApiResponse<Map<String, Object>> getDeliveryStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Double distanceKm) {
        PharmacyDto p = pharmacyService.getPharmacyById(id);
        DeliveryStatus status = deliveryQuery != null ? deliveryQuery.statusFor(id) : DeliveryStatus.UNAVAILABLE;
        Integer eta = null;
        if ((status == DeliveryStatus.AVAILABLE || status == DeliveryStatus.BUSY) && (distanceKm != null || p.distanceKm() != null)) {
            double dist = distanceKm != null ? distanceKm : (p.distanceKm() != null ? p.distanceKm() : 0.0);
            eta = Math.min(120, Math.max(20, 20 + (int) Math.round(dist * 4.0)));
        }
        return ApiResponse.ok(Map.of(
                "pharmacyId", id,
                "pharmacyName", p.name(),
                "deliveryAvailable", Boolean.TRUE.equals(p.deliveryAvailable()),
                "deliveryStatus", status.name(),
                "deliveryRadiusKm", p.deliveryRadiusKm() != null ? p.deliveryRadiusKm() : 0.0,
                "deliveryFee", p.deliveryFee() != null ? p.deliveryFee() : 0.0,
                "estimatedDeliveryMinutes", eta != null ? eta : 0,
                "selfPickupAvailable", true
        ));
    }

    @GetMapping("/{id}/inventory")
    public ApiResponse<PageResponse<InventoryRowResponse>> getPharmacyInventory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(PageResponse.from(inventoryService.getAdminInventoryByPharmacy(id, PageRequest.of(page, size))));
    }

    @PostMapping("/{id}/inventory")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InventoryRowResponse> addPharmacyInventory(
            @PathVariable Long id,
            @Valid @RequestBody AddInventoryRequest req) {
        return ApiResponse.ok(inventoryService.addStock(req), "Stock added to pharmacy inventory");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PharmacyDto> registerPharmacy(@Valid @RequestBody RegisterPharmacyRequest req) {
        return ApiResponse.ok(pharmacyService.registerPharmacy(req), "Pharmacy registration submitted for verification");
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PharmacyDto> registerPharmacyExplicit(@Valid @RequestBody RegisterPharmacyRequest req) {
        return ApiResponse.ok(pharmacyService.registerPharmacy(req), "Pharmacy registration submitted for verification");
    }

    /**
     * Admin endpoints: review pending pharmacies, approve or reject.
     */
    @GetMapping("/pending-approval")
    public ApiResponse<List<PharmacyDto>> getPendingPharmacies() {
        return ApiResponse.ok(pharmacyService.getPendingPharmacies());
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<PharmacyDto> approvePharmacy(@PathVariable Long id) {
        return ApiResponse.ok(pharmacyService.approvePharmacy(id), "Pharmacy verified and approved successfully");
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<PharmacyDto> rejectPharmacy(@PathVariable Long id, @RequestParam(defaultValue = "License or details unverified") String reason) {
        return ApiResponse.ok(pharmacyService.rejectPharmacy(id, reason), "Pharmacy registration rejected");
    }
}
