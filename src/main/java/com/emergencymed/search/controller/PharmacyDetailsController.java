package com.emergencymed.search.controller;

import com.emergencymed.common.contract.InventoryQueryService;
import com.emergencymed.common.contract.MedicineLookup;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.common.dto.ApiResponse;
import com.emergencymed.common.dto.MedicineDto;
import com.emergencymed.common.dto.MedicineOfferDto;
import com.emergencymed.common.dto.PharmacyDto;
import com.emergencymed.common.exception.ResourceNotFoundException;
import com.emergencymed.common.util.GeoUtils;
import com.emergencymed.search.dto.PharmacyMatch;
import com.emergencymed.search.service.PharmacySearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pharmacies")
public class PharmacyDetailsController {

    private final PharmacyLookup pharmacyLookup;
    private final MedicineLookup medicineLookup;
    private final InventoryQueryService inventoryQueryService;
    private final PharmacySearchService pharmacySearchService;

    @Autowired
    public PharmacyDetailsController(PharmacyLookup pharmacyLookup,
                                     MedicineLookup medicineLookup,
                                     InventoryQueryService inventoryQueryService,
                                     PharmacySearchService pharmacySearchService) {
        this.pharmacyLookup = pharmacyLookup;
        this.medicineLookup = medicineLookup;
        this.inventoryQueryService = inventoryQueryService;
        this.pharmacySearchService = pharmacySearchService;
    }

    /**
     * GET /api/v1/pharmacies/{id}/details
     * Fetches detailed profile of a pharmacy along with current medicine offer and contact links.
     */
    @GetMapping("/{id}/details")
    public ApiResponse<PharmacyMatch> getPharmacyDetails(
            @PathVariable("id") Long id,
            @RequestParam(value = "medicineId", required = false) Long medicineId,
            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lng", required = false) Double lng) {

        PharmacyDto pharmacy = pharmacyLookup.findApproved(id).orElse(null);
        if (pharmacy == null) {
            throw new ResourceNotFoundException("Approved pharmacy not found for ID: " + id);
        }

        double userLat = lat != null ? lat : pharmacy.getLatitude();
        double userLng = lng != null ? lng : pharmacy.getLongitude();
        double distance = GeoUtils.haversineKm(userLat, userLng, pharmacy.getLatitude(), pharmacy.getLongitude());

        MedicineDto medicine = null;
        if (medicineId != null) {
            medicine = medicineLookup.findById(medicineId).orElse(null);
        }
        if (medicine == null) {
            // Default placeholder medicine info
            medicine = new MedicineDto(0L, "General Inquiry", "N/A", "N/A", "N/A", "N/A", "", false, true);
        }

        MedicineOfferDto offer = null;
        if (medicineId != null) {
            List<MedicineOfferDto> offers = inventoryQueryService.findOffers(medicineId, 1, Collections.singletonList(id));
            if (!offers.isEmpty()) {
                offer = offers.get(0);
            }
        }
        if (offer == null) {
            offer = new MedicineOfferDto(
                    0L, id, medicine.getId(), 0, 0, java.math.BigDecimal.ZERO,
                    com.emergencymed.common.dto.StockStatus.OUT_OF_STOCK, "SAMPLE",
                    java.time.Instant.now().toString(), null, "N/A"
            );
        }

        PharmacyMatch match = pharmacySearchService.buildMatch(pharmacy, offer, medicine, quantity, userLat, userLng, distance);
        return ApiResponse.ok("Pharmacy details retrieved successfully", match);
    }
}
