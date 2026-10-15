package com.emergencymed.search.service;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.contract.InventoryQueryService;
import com.emergencymed.common.contract.MedicineLookup;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.common.dto.*;
import com.emergencymed.common.enums.Role;
import com.emergencymed.common.exception.ForbiddenException;
import com.emergencymed.common.exception.ResourceNotFoundException;
import com.emergencymed.common.exception.ValidationException;
import com.emergencymed.common.util.GeoUtils;
import com.emergencymed.search.dto.CompareResponse;
import com.emergencymed.search.dto.PharmacyMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ComparisonService {

    private final PharmacyLookup pharmacyLookup;
    private final MedicineLookup medicineLookup;
    private final InventoryQueryService inventoryQueryService;
    private final PharmacySearchService pharmacySearchService;
    private final CurrentUserProvider currentUserProvider;

    @Autowired
    public ComparisonService(PharmacyLookup pharmacyLookup,
                             MedicineLookup medicineLookup,
                             InventoryQueryService inventoryQueryService,
                             PharmacySearchService pharmacySearchService,
                             CurrentUserProvider currentUserProvider) {
        this.pharmacyLookup = pharmacyLookup;
        this.medicineLookup = medicineLookup;
        this.inventoryQueryService = inventoryQueryService;
        this.pharmacySearchService = pharmacySearchService;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Compares 2 to 3 distinct approved pharmacies side-by-side.
     */
    public CompareResponse comparePharmacies(List<Long> pharmacyIds, Long medicineId,
                                            Integer quantity, Double lat, Double lng) {
        // Enforce role authorization
        if (currentUserProvider.isAuthenticated()) {
            Role role = currentUserProvider.role();
            if (role != Role.PATIENT && role != Role.HOSPITAL && role != Role.ADMIN) {
                throw new ForbiddenException("Role " + role + " is not authorized to compare pharmacies");
            }
        }

        if (lat == null || lng == null) {
            throw new ValidationException("Location is required");
        }
        if (medicineId == null) {
            throw new ValidationException("Medicine ID is required");
        }
        int qty = (quantity != null && quantity > 0) ? quantity : 1;

        // Validation: 2 to 3 distinct IDs
        if (pharmacyIds == null || pharmacyIds.size() < 2 || pharmacyIds.size() > 3) {
            throw new ValidationException("Comparison requires between 2 and 3 pharmacy IDs");
        }
        Set<Long> uniqueIds = new HashSet<>(pharmacyIds);
        if (uniqueIds.size() != pharmacyIds.size()) {
            throw new ValidationException("Comparison pharmacies must be distinct");
        }

        MedicineDto medicine = medicineLookup.findById(medicineId).orElse(null);
        if (medicine == null || !medicine.isActive()) {
            throw new ResourceNotFoundException("Active medicine not found for ID: " + medicineId);
        }

        List<MedicineOfferDto> offers = inventoryQueryService.findOffers(medicineId, 1, pharmacyIds);
        Map<Long, MedicineOfferDto> offerMap = new HashMap<>();
        for (MedicineOfferDto o : offers) {
            offerMap.put(o.getPharmacyId(), o);
        }

        List<PharmacyMatch> matches = new ArrayList<>();
        for (Long pId : pharmacyIds) {
            PharmacyDto pharm = pharmacyLookup.findApproved(pId).orElse(null);
            if (pharm == null) {
                throw new ValidationException("Pharmacy with ID " + pId + " is not found or not approved");
            }

            MedicineOfferDto offer = offerMap.get(pId);
            if (offer == null) {
                // If specific offer not found, provide fallback placeholder offer for that approved pharmacy
                offer = new MedicineOfferDto(
                        0L, pId, medicineId, 0, 0, java.math.BigDecimal.ZERO,
                        StockStatus.OUT_OF_STOCK, "SAMPLE", java.time.Instant.now().toString(), null, "N/A"
                );
            }

            double dist = GeoUtils.haversineKm(lat, lng, pharm.getLatitude(), pharm.getLongitude());
            PharmacyMatch match = pharmacySearchService.buildMatch(pharm, offer, medicine, qty, lat, lng, dist);
            matches.add(match);
        }

        return new CompareResponse(matches, medicineId, qty);
    }
}
