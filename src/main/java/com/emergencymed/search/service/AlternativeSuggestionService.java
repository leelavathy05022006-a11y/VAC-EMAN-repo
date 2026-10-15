package com.emergencymed.search.service;

import com.emergencymed.common.contract.DeliveryAvailabilityQuery;
import com.emergencymed.common.contract.InventoryQueryService;
import com.emergencymed.common.contract.MedicineLookup;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.common.contract.PharmacySettingsLookup;
import com.emergencymed.common.dto.*;
import com.emergencymed.common.enums.DeliveryStatus;
import com.emergencymed.common.util.GeoUtils;
import com.emergencymed.common.util.MapLinks;
import com.emergencymed.common.util.PhoneUtils;
import com.emergencymed.search.dto.AlternativesResponse;
import com.emergencymed.search.dto.ContactInfo;
import com.emergencymed.search.dto.DeliveryInfo;
import com.emergencymed.search.dto.PharmacyMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlternativeSuggestionService {

    private final PharmacyLookup pharmacyLookup;
    private final InventoryQueryService inventoryQueryService;
    private final PharmacySettingsLookup pharmacySettingsLookup;
    private final DeliveryAvailabilityQuery deliveryAvailabilityQuery;
    private final OpeningHoursService openingHoursService;
    private final MedicineLookup medicineLookup;

    @Autowired
    public AlternativeSuggestionService(PharmacyLookup pharmacyLookup,
                                       InventoryQueryService inventoryQueryService,
                                       PharmacySettingsLookup pharmacySettingsLookup,
                                       DeliveryAvailabilityQuery deliveryAvailabilityQuery,
                                       OpeningHoursService openingHoursService,
                                       MedicineLookup medicineLookup) {
        this.pharmacyLookup = pharmacyLookup;
        this.inventoryQueryService = inventoryQueryService;
        this.pharmacySettingsLookup = pharmacySettingsLookup;
        this.deliveryAvailabilityQuery = deliveryAvailabilityQuery;
        this.openingHoursService = openingHoursService;
        this.medicineLookup = medicineLookup;
    }

    /**
     * Builds the alternatives response when zero direct matches are found:
     * 1. Wider radius: radius * 2.5 (capped at 50 km).
     * 2. Partial stock: pharmacies with some stock (> 0) but less than requested quantity.
     * 3. Generic substitutes: medicines sharing the same generic compound.
     */
    public AlternativesResponse findAlternatives(Long medicineId, int requestedQty,
                                                 double userLat, double userLng, double originalRadiusKm) {
        MedicineDto requestedMed = medicineLookup.findById(medicineId).orElse(null);
        boolean requiresPrescription = requestedMed != null && requestedMed.isRequiresPrescription();
        String medicineName = requestedMed != null ? requestedMed.getName() : "";

        // 1. Wider radius search: 2.5x original radius, capped at 50 km
        double widerRadiusKm = Math.min(originalRadiusKm * 2.5, 50.0);
        List<PharmacyDto> widerPharmacies = pharmacyLookup.findApprovedWithin(userLat, userLng, widerRadiusKm);
        List<Long> widerPharmacyIds = widerPharmacies.stream().map(PharmacyDto::getId).collect(Collectors.toList());

        List<PharmacyMatch> widerRadiusMatches = new ArrayList<>();
        if (!widerPharmacyIds.isEmpty()) {
            List<MedicineOfferDto> offers = inventoryQueryService.findOffers(medicineId, requestedQty, widerPharmacyIds);
            Map<Long, PharmacyDto> pharmMap = widerPharmacies.stream()
                    .collect(Collectors.toMap(PharmacyDto::getId, p -> p, (a, b) -> a));

            for (MedicineOfferDto offer : offers) {
                PharmacyDto pharm = pharmMap.get(offer.getPharmacyId());
                if (pharm != null) {
                    double dist = GeoUtils.haversineKm(userLat, userLng, pharm.getLatitude(), pharm.getLongitude());
                    // Only include pharmacies that were outside the original radius
                    if (dist > originalRadiusKm) {
                        PharmacyMatch match = buildPharmacyMatch(pharm, offer, requestedQty, userLat, userLng, dist, requiresPrescription, medicineName);
                        widerRadiusMatches.add(match);
                    }
                }
            }
            widerRadiusMatches.sort(Comparator.comparing(PharmacyMatch::getDistanceKm));
        }

        // 2. Partial stock and generic substitutes from findAlternatives
        List<PharmacyDto> nearbyPharmacies = pharmacyLookup.findApprovedWithin(userLat, userLng, widerRadiusKm);
        List<Long> nearbyPharmacyIds = nearbyPharmacies.stream().map(PharmacyDto::getId).collect(Collectors.toList());
        Map<Long, PharmacyDto> allNearbyMap = nearbyPharmacies.stream()
                .collect(Collectors.toMap(PharmacyDto::getId, p -> p, (a, b) -> a));

        AlternativesDto altDto = inventoryQueryService.findAlternatives(medicineId, nearbyPharmacyIds);

        List<PharmacyMatch> partialStockMatches = new ArrayList<>();
        if (altDto != null && altDto.getPartialStockOffers() != null) {
            LocalDate today = LocalDate.now();
            for (MedicineOfferDto offer : altDto.getPartialStockOffers()) {
                if (offer.getExpiryDate() != null && offer.getExpiryDate().isBefore(today)) {
                    continue;
                }
                int available = offer.getAvailableQuantity() - (offer.getReservedQuantity() != null ? offer.getReservedQuantity() : 0);
                if (available > 0 && available < requestedQty) {
                    PharmacyDto pharm = allNearbyMap.get(offer.getPharmacyId());
                    if (pharm != null) {
                        double dist = GeoUtils.haversineKm(userLat, userLng, pharm.getLatitude(), pharm.getLongitude());
                        PharmacyMatch match = buildPharmacyMatch(pharm, offer, available, userLat, userLng, dist, requiresPrescription, medicineName);
                        partialStockMatches.add(match);
                    }
                }
            }
            partialStockMatches.sort(Comparator.comparing(PharmacyMatch::getDistanceKm));
        }

        List<MedicineDto> genericSubstitutes = (altDto != null && altDto.getGenericSubstitutes() != null)
                ? altDto.getGenericSubstitutes()
                : Collections.emptyList();

        return new AlternativesResponse(
                "No direct matches found within " + originalRadiusKm + " km for quantity " + requestedQty + ". Showing recommendations:",
                widerRadiusMatches,
                partialStockMatches,
                genericSubstitutes
        );
    }

    private PharmacyMatch buildPharmacyMatch(PharmacyDto pharm, MedicineOfferDto offer, int qty,
                                            double userLat, double userLng, double dist,
                                            boolean requiresPrescription, String medicineName) {
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

        int avail = offer.getAvailableQuantity() - (offer.getReservedQuantity() != null ? offer.getReservedQuantity() : 0);
        match.setAvailableQuantity(avail);
        match.setUnitPrice(offer.getUnitPrice());
        match.setTotalPrice(offer.getUnitPrice() != null ? offer.getUnitPrice().multiply(BigDecimal.valueOf(qty)) : BigDecimal.ZERO);
        match.setStockStatus(offer.getStockStatus() != null ? offer.getStockStatus().name() : "IN_STOCK");
        match.setDataSource(offer.getDataSource() != null ? offer.getDataSource() : "SAMPLE");
        match.setLastUpdatedAt(offer.getLastUpdatedAt());
        match.setExpiryDate(offer.getExpiryDate());
        match.setRequiresPrescription(requiresPrescription);

        // Delivery info with fallback
        DeliveryStatus delStatus = DeliveryStatus.UNAVAILABLE;
        try {
            if (deliveryAvailabilityQuery != null) {
                delStatus = deliveryAvailabilityQuery.statusFor(pharm.getId());
            }
        } catch (Exception e) {
            delStatus = DeliveryStatus.UNAVAILABLE;
        }

        PharmacySettingsDto settings = pharmacySettingsLookup != null ? pharmacySettingsLookup.find(pharm.getId()).orElse(null) : null;
        BigDecimal fee = (settings != null && settings.getDeliveryFee() != null) ? settings.getDeliveryFee() : BigDecimal.ZERO;
        Double radius = (settings != null && settings.getDeliveryRadiusKm() != null) ? settings.getDeliveryRadiusKm().doubleValue() : 10.0;
        match.setDelivery(new DeliveryInfo(delStatus.name(), fee, radius));

        // Contact info
        String callUrl = PhoneUtils.formatTelUrl(pharm.getPhoneNumber());
        String waUrl = PhoneUtils.formatWhatsappUrl(pharm.getPhoneNumber(), medicineName, qty);
        match.setContact(new ContactInfo(callUrl, waUrl));

        // Navigation URL
        match.setNavigationUrl(MapLinks.buildDirectionsUrl(userLat, userLng, pharm.getLatitude(), pharm.getLongitude(), "driving"));

        return match;
    }
}
