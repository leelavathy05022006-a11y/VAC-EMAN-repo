package com.emergencymed.search.service;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.contract.DeliveryAvailabilityQuery;
import com.emergencymed.common.contract.InventoryQueryService;
import com.emergencymed.common.contract.MedicineLookup;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.common.contract.PharmacySettingsLookup;
import com.emergencymed.common.dto.*;
import com.emergencymed.common.enums.DeliveryStatus;
import com.emergencymed.common.enums.Role;
import com.emergencymed.common.exception.ForbiddenException;
import com.emergencymed.common.exception.ResourceNotFoundException;
import com.emergencymed.common.exception.ValidationException;
import com.emergencymed.common.util.GeoUtils;
import com.emergencymed.common.util.MapLinks;
import com.emergencymed.common.util.PhoneUtils;
import com.emergencymed.search.dto.ContactInfo;
import com.emergencymed.search.dto.DeliveryInfo;
import com.emergencymed.search.dto.PharmacyMatch;
import com.emergencymed.search.dto.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PharmacySearchService {

    private final PharmacyLookup pharmacyLookup;
    private final MedicineLookup medicineLookup;
    private final InventoryQueryService inventoryQueryService;
    private final PharmacySettingsLookup pharmacySettingsLookup;
    private final DeliveryAvailabilityQuery deliveryAvailabilityQuery;
    private final OpeningHoursService openingHoursService;
    private final SearchHistoryService searchHistoryService;
    private final CurrentUserProvider currentUserProvider;

    @Autowired
    public PharmacySearchService(PharmacyLookup pharmacyLookup,
                                 MedicineLookup medicineLookup,
                                 InventoryQueryService inventoryQueryService,
                                 PharmacySettingsLookup pharmacySettingsLookup,
                                 DeliveryAvailabilityQuery deliveryAvailabilityQuery,
                                 OpeningHoursService openingHoursService,
                                 SearchHistoryService searchHistoryService,
                                 CurrentUserProvider currentUserProvider) {
        this.pharmacyLookup = pharmacyLookup;
        this.medicineLookup = medicineLookup;
        this.inventoryQueryService = inventoryQueryService;
        this.pharmacySettingsLookup = pharmacySettingsLookup;
        this.deliveryAvailabilityQuery = deliveryAvailabilityQuery;
        this.openingHoursService = openingHoursService;
        this.searchHistoryService = searchHistoryService;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Executes the medicine availability search across approved pharmacies.
     */
    public List<PharmacyMatch> searchPharmacies(SearchRequest request) {
        validateSearchRequest(request);

        // Verify role authorization: only PATIENT, HOSPITAL, ADMIN allowed (allow unauthenticated guests for public search / chatbot)
        if (currentUserProvider.isAuthenticated()) {
            Role userRole = currentUserProvider.role();
            if (userRole != Role.PATIENT && userRole != Role.HOSPITAL && userRole != Role.ADMIN) {
                throw new ForbiddenException("Role " + userRole + " is not authorized to perform medicine searches");
            }
        }

        // Verify medicine exists and is active; otherwise 404 NOT_FOUND
        MedicineDto medicine = medicineLookup.findById(request.getMedicineId()).orElse(null);
        if (medicine == null || !medicine.isActive()) {
            throw new ResourceNotFoundException("Medicine with ID " + request.getMedicineId() + " not found or is inactive");
        }

        double radius = request.getRadiusKm() != null ? request.getRadiusKm() : 10.0;
        List<PharmacyDto> nearbyPharmacies = pharmacyLookup.findApprovedWithin(request.getLat(), request.getLng(), radius);

        List<PharmacyMatch> matches = new ArrayList<>();
        if (!nearbyPharmacies.isEmpty()) {
            List<Long> pharmacyIds = nearbyPharmacies.stream().map(PharmacyDto::getId).collect(Collectors.toList());
            List<MedicineOfferDto> offers = inventoryQueryService.findOffers(request.getMedicineId(), request.getQuantity(), pharmacyIds);

            Map<Long, PharmacyDto> pharmMap = nearbyPharmacies.stream()
                    .collect(Collectors.toMap(PharmacyDto::getId, p -> p, (a, b) -> a));

            for (MedicineOfferDto offer : offers) {
                PharmacyDto pharm = pharmMap.get(offer.getPharmacyId());
                if (pharm != null) {
                    double dist = GeoUtils.haversineKm(request.getLat(), request.getLng(), pharm.getLatitude(), pharm.getLongitude());
                    PharmacyMatch match = buildMatch(pharm, offer, medicine, request.getQuantity(), request.getLat(), request.getLng(), dist);
                    matches.add(match);
                }
            }
        }

        // Apply deliveryOnly filter
        if (Boolean.TRUE.equals(request.getDeliveryOnly())) {
            matches = matches.stream()
                    .filter(m -> "AVAILABLE".equalsIgnoreCase(m.getDelivery().getStatus()) || "BUSY".equalsIgnoreCase(m.getDelivery().getStatus()))
                    .collect(Collectors.toList());
        }

        // Apply sorting
        sortMatches(matches, request.getSort());

        // Log search in history
        searchHistoryService.logSearch(
                request.getMedicineId(),
                medicine.getName(),
                request.getQuantity(),
                request.getLat(),
                request.getLng(),
                radius,
                request.getSort(),
                matches.size()
        );

        return matches;
    }

    private void validateSearchRequest(SearchRequest request) {
        if (request.getMedicineId() == null) {
            throw new ValidationException("Medicine ID is required");
        }
        if (request.getQuantity() == null || request.getQuantity() < 1 || request.getQuantity() > 1000) {
            throw new ValidationException("Quantity must be between 1 and 1000");
        }
        if (request.getLat() == null || request.getLng() == null) {
            throw new ValidationException("Location is required");
        }
        if (request.getLat() < -90.0 || request.getLat() > 90.0 || request.getLng() < -180.0 || request.getLng() > 180.0) {
            throw new ValidationException("Location is required and must be within valid geographic range");
        }
        if (request.getRadiusKm() != null && (request.getRadiusKm() < 1.0 || request.getRadiusKm() > 50.0)) {
            throw new ValidationException("Radius must be between 1 and 50 km");
        }
        if (request.getSort() != null && !request.getSort().isBlank()) {
            String s = request.getSort().trim().toLowerCase();
            if (!s.equals("distance") && !s.equals("price") && !s.equals("quantity") && !s.equals("delivery")) {
                throw new ValidationException("Sort must be one of: distance, price, quantity, delivery");
            }
        }
    }

    private void sortMatches(List<PharmacyMatch> matches, String sort) {
        String sortBy = (sort != null && !sort.isBlank()) ? sort.trim().toLowerCase() : "distance";
        switch (sortBy) {
            case "price" -> matches.sort(Comparator.comparing(PharmacyMatch::getUnitPrice));
            case "quantity" -> matches.sort(Comparator.comparing(PharmacyMatch::getAvailableQuantity).reversed());
            case "delivery" -> matches.sort((a, b) -> {
                boolean aDel = "AVAILABLE".equalsIgnoreCase(a.getDelivery().getStatus()) || "BUSY".equalsIgnoreCase(a.getDelivery().getStatus());
                boolean bDel = "AVAILABLE".equalsIgnoreCase(b.getDelivery().getStatus()) || "BUSY".equalsIgnoreCase(b.getDelivery().getStatus());
                if (aDel != bDel) {
                    return aDel ? -1 : 1;
                }
                return Double.compare(a.getDistanceKm(), b.getDistanceKm());
            });
            case "distance" -> matches.sort(Comparator.comparing(PharmacyMatch::getDistanceKm));
            default -> matches.sort(Comparator.comparing(PharmacyMatch::getDistanceKm));
        }
    }

    public PharmacyMatch buildMatch(PharmacyDto pharm, MedicineOfferDto offer, MedicineDto med,
                                    int qty, double userLat, double userLng, double dist) {
        PharmacyMatch match = new PharmacyMatch();
        match.setPharmacyId(pharm.getId());
        match.setName(pharm.getName());
        match.setAddress(pharm.getAddress());
        match.setDistanceKm(BigDecimal.valueOf(dist).setScale(1, RoundingMode.HALF_UP).doubleValue());
        match.setEtaMinutes((int) Math.round((dist / 25.0) * 60));
        match.setOpenNow(openingHoursService.isOpenNow(pharm));
        match.setOpeningTime(pharm.getOpeningTime());
        match.setClosingTime(pharm.getClosingTime());
        match.setOpens24x7(pharm.isOpens24x7());
        match.setInventoryId(offer.getInventoryId());

        int available = offer.getAvailableQuantity() - (offer.getReservedQuantity() != null ? offer.getReservedQuantity() : 0);
        match.setAvailableQuantity(available);
        match.setUnitPrice(offer.getUnitPrice());
        match.setTotalPrice(offer.getUnitPrice() != null ? offer.getUnitPrice().multiply(BigDecimal.valueOf(qty)) : BigDecimal.ZERO);
        match.setStockStatus(offer.getStockStatus() != null ? offer.getStockStatus().name() : "IN_STOCK");
        match.setDataSource(offer.getDataSource() != null ? offer.getDataSource() : "SAMPLE");
        match.setLastUpdatedAt(offer.getLastUpdatedAt());
        match.setExpiryDate(offer.getExpiryDate());
        match.setRequiresPrescription(med.isRequiresPrescription());

        // Delivery lookup with resilient fallback
        DeliveryStatus status = DeliveryStatus.UNAVAILABLE;
        try {
            if (deliveryAvailabilityQuery != null) {
                status = deliveryAvailabilityQuery.statusFor(pharm.getId());
            }
        } catch (Exception e) {
            status = DeliveryStatus.UNAVAILABLE;
        }

        PharmacySettingsDto settings = pharmacySettingsLookup != null ? pharmacySettingsLookup.find(pharm.getId()).orElse(null) : null;
        BigDecimal fee = (settings != null && settings.getDeliveryFee() != null) ? settings.getDeliveryFee() : BigDecimal.ZERO;
        Double radius = (settings != null && settings.getDeliveryRadiusKm() != null) ? settings.getDeliveryRadiusKm().doubleValue() : 10.0;
        match.setDelivery(new DeliveryInfo(status.name(), fee, radius));

        // Contact details - protected against unauthenticated exposure
        if (currentUserProvider.isAuthenticated()) {
            String callUrl = PhoneUtils.formatTelUrl(pharm.getPhoneNumber());
            String waUrl = PhoneUtils.formatWhatsappUrl(pharm.getPhoneNumber(), med.getName(), qty);
            match.setContact(new ContactInfo(callUrl, waUrl));
        } else {
            match.setContact(new ContactInfo(null, null));
        }

        // Navigation URL
        match.setNavigationUrl(MapLinks.buildDirectionsUrl(userLat, userLng, pharm.getLatitude(), pharm.getLongitude(), "driving"));

        return match;
    }
}
