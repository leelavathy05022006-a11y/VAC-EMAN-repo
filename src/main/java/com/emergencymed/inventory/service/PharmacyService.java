package com.emergencymed.inventory.service;

import com.emergencymed.common.contract.DeliveryAvailabilityQuery;
import com.emergencymed.common.enums.DeliveryStatus;
import com.emergencymed.common.events.PharmacyApprovedEvent;
import com.emergencymed.inventory.dto.*;
import com.emergencymed.auth.entity.Pharmacy;
import com.emergencymed.inventory.entity.PharmacyInventory;
import com.emergencymed.inventory.entity.PharmacySettings;
import com.emergencymed.inventory.exception.ResourceNotFoundException;
import com.emergencymed.inventory.repository.PharmacyInventoryRepository;
import com.emergencymed.auth.repository.PharmacyRepository;
import com.emergencymed.inventory.repository.PharmacySettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PharmacyService {
    private static final Logger log = LoggerFactory.getLogger(PharmacyService.class);

    private final PharmacyRepository pharmacyRepository;
    private final PharmacySettingsRepository settingsRepository;
    private final PharmacyInventoryRepository inventoryRepository;
    private final DeliveryAvailabilityQuery deliveryQuery;
    private final ExternalPharmacyDirectoryService externalDirectoryService;
    private final ApplicationEventPublisher eventPublisher;

    public PharmacyService(PharmacyRepository pharmacyRepository,
                           PharmacySettingsRepository settingsRepository,
                           PharmacyInventoryRepository inventoryRepository,
                           DeliveryAvailabilityQuery deliveryQuery,
                           ExternalPharmacyDirectoryService externalDirectoryService,
                           ApplicationEventPublisher eventPublisher) {
        this.pharmacyRepository = pharmacyRepository;
        this.settingsRepository = settingsRepository;
        this.inventoryRepository = inventoryRepository;
        this.deliveryQuery = deliveryQuery;
        this.externalDirectoryService = externalDirectoryService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Discovery around user coordinates with radius and advanced filters.
     */
    @Transactional(readOnly = true)
    public PharmacyDiscoveryResponse discoverNearbyPharmacies(double lat, double lng, double radiusKm,
                                                              String localityFilter,
                                                              Boolean verifiedStockOnly,
                                                              Boolean deliveryOnly,
                                                              Boolean open24x7Only) {
        return discoverNearbyPharmacies(lat, lng, radiusKm, localityFilter, null, verifiedStockOnly, deliveryOnly, open24x7Only);
    }

    @Transactional(readOnly = true)
    public PharmacyDiscoveryResponse discoverNearbyPharmacies(double lat, double lng, double radiusKm,
                                                              String localityFilter,
                                                              String facilityTypeFilter,
                                                              Boolean verifiedStockOnly,
                                                              Boolean deliveryOnly,
                                                              Boolean open24x7Only) {
        double maxRadius = radiusKm > 0 ? radiusKm : 10.0;
        List<Pharmacy> all = pharmacyRepository.findAll();

        // Map and calculate distance
        List<PharmacyDto> dtos = all.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                // Only show approved pharmacies in public discovery
                .filter(p -> Boolean.TRUE.equals(p.getIsApproved()))
                .map(p -> {
                    double dist = calculateHaversineDistance(lat, lng, p.getLatitude().doubleValue(), p.getLongitude().doubleValue());
                    return toDto(p, dist);
                })
                .filter(dto -> dto.distanceKm() <= maxRadius)
                .filter(dto -> localityFilter == null || localityFilter.isBlank() ||
                        dto.locality().equalsIgnoreCase(localityFilter.trim()))
                .filter(dto -> facilityTypeFilter == null || facilityTypeFilter.isBlank() || "ALL".equalsIgnoreCase(facilityTypeFilter) ||
                        dto.facilityType().equalsIgnoreCase(facilityTypeFilter.trim()))
                .filter(dto -> verifiedStockOnly == null || !verifiedStockOnly ||
                        "VERIFIED_LIVE".equals(dto.stockStatus()) || "SAMPLE_DEMO".equals(dto.stockStatus()))
                .filter(dto -> deliveryOnly == null || !deliveryOnly ||
                        Boolean.TRUE.equals(dto.deliveryAvailable()) || "PARTNER_AVAILABLE".equals(dto.deliveryStatus()))
                .filter(dto -> open24x7Only == null || !open24x7Only ||
                        Boolean.TRUE.equals(dto.opens24x7()) || (dto.openingHours() != null && dto.openingHours().contains("24x7")))
                .sorted(Comparator.comparingDouble(PharmacyDto::distanceKm))
                .collect(Collectors.toList());

        // Reverse geocode detected locality
        String detectedLocality = externalDirectoryService.reverseGeocode(lat, lng);

        String coverageNote = "Directory coverage aggregates verified network pharmacies, hospitals, clinics and landmarks across Chennai. Unlisted facilities can register for administrative verification.";

        return new PharmacyDiscoveryResponse(
                lat,
                lng,
                maxRadius,
                detectedLocality,
                detectedLocality,
                dtos.size(),
                coverageNote,
                dtos
        );
    }

    @Transactional(readOnly = true)
    public List<PharmacyDto> getNearbyPharmacies(double lat, double lng, double radiusKm) {
        PharmacyDiscoveryResponse resp = discoverNearbyPharmacies(lat, lng, radiusKm, null, false, false, false);
        return resp.pharmacies();
    }

    @Transactional(readOnly = true)
    public List<PharmacyDto> getPharmacies(String city, String locality) {
        String targetCity = (city != null && !city.isBlank()) ? city : "Chennai";
        List<Pharmacy> list;
        if (locality != null && !locality.isBlank()) {
            list = pharmacyRepository.findByCityIgnoreCaseAndLocalityIgnoreCaseAndIsActiveTrue(targetCity, locality);
        } else {
            list = pharmacyRepository.findByCityIgnoreCaseAndIsActiveTrue(targetCity);
        }
        return list.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsApproved()))
                .map(p -> toDto(p, null))
                .collect(Collectors.toList());
    }

    /**
     * Granular Hyperlocal search by street, area, landmark, or PIN code with facility type filter.
     */
    @Transactional(readOnly = true)
    public List<PharmacyDto> searchHyperlocal(String query, Double lat, Double lng, Double radiusKm) {
        return searchHyperlocal(query, lat, lng, radiusKm, null);
    }

    @Transactional(readOnly = true)
    public List<PharmacyDto> searchHyperlocal(String query, Double lat, Double lng, Double radiusKm, String facilityType) {
        if (query == null || query.isBlank()) {
            if (lat != null && lng != null) {
                return discoverNearbyPharmacies(lat, lng, radiusKm != null ? radiusKm : 10.0, null, facilityType, false, false, false).pharmacies();
            }
            List<PharmacyDto> allDtos = getPharmacies("Chennai", null);
            if (facilityType != null && !facilityType.isBlank() && !"ALL".equalsIgnoreCase(facilityType)) {
                return allDtos.stream()
                        .filter(p -> p.facilityType().equalsIgnoreCase(facilityType.trim()))
                        .collect(Collectors.toList());
            }
            return allDtos;
        }

        String q = query.trim();
        List<Pharmacy> matched = pharmacyRepository.searchAllMatching(q);

        // Deduplicate by ID
        Map<Long, Pharmacy> uniqueMap = new LinkedHashMap<>();
        for (Pharmacy p : matched) {
            if (Boolean.TRUE.equals(p.getIsApproved())) {
                if (facilityType == null || facilityType.isBlank() || "ALL".equalsIgnoreCase(facilityType) ||
                        (p.getFacilityType() != null && p.getFacilityType().equalsIgnoreCase(facilityType.trim()))) {
                    uniqueMap.put(p.getId(), p);
                }
            }
        }

        List<PharmacyDto> results = uniqueMap.values().stream()
                .map(p -> {
                    Double dist = (lat != null && lng != null) ?
                            calculateHaversineDistance(lat, lng, p.getLatitude().doubleValue(), p.getLongitude().doubleValue()) :
                            null;
                    return toDto(p, dist);
                })
                .sorted((a, b) -> {
                    if (a.distanceKm() != null && b.distanceKm() != null) {
                        return Double.compare(a.distanceKm(), b.distanceKm());
                    }
                    return a.name().compareToIgnoreCase(b.name());
                })
                .collect(Collectors.toList());

        return results;
    }

    @Transactional(readOnly = true)
    public PharmacyDto getPharmacyById(Long id, Double userLat, Double userLng) {
        Pharmacy p = pharmacyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found with ID: " + id));
        Double dist = (userLat != null && userLng != null) ?
                calculateHaversineDistance(userLat, userLng, p.getLatitude().doubleValue(), p.getLongitude().doubleValue()) :
                null;
        return toDto(p, dist);
    }

    @Transactional(readOnly = true)
    public PharmacyDto getPharmacyById(Long id) {
        return getPharmacyById(id, null, null);
    }

    /**
     * Get real medicine availability for a specific pharmacy.
     * Accurately calculates available quantity = quantity - reserved quantity.
     * Excludes or flags expired items.
     */
    @Transactional(readOnly = true)
    public List<MedicineAvailabilityItemDto> getMedicineAvailability(Long pharmacyId, String query) {
        // Ensure pharmacy exists
        pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found with ID: " + pharmacyId));

        List<PharmacyInventory> inventoryList = inventoryRepository.findByPharmacyIdAndIsActiveTrue(pharmacyId);
        LocalDate today = LocalDate.now();

        String q = query != null ? query.trim().toLowerCase() : "";

        return inventoryList.stream()
                .filter(inv -> {
                    if (q.isBlank()) return true;
                    String medName = inv.getMedicine().getName().toLowerCase();
                    String genName = inv.getMedicine().getGenericName().toLowerCase();
                    return medName.contains(q) || genName.contains(q);
                })
                .map(inv -> {
                    int available = inv.getAvailableQuantity();
                    boolean isExpired = inv.getExpiryDate() != null && inv.getExpiryDate().isBefore(today);
                    boolean isNearExpiry = !isExpired && inv.getExpiryDate() != null &&
                            inv.getExpiryDate().isBefore(today.plusDays(30));

                    String status;
                    if (isExpired) {
                        status = "EXPIRED";
                    } else if (available <= 0) {
                        status = "OUT_OF_STOCK";
                    } else if (available <= inv.getLowStockThreshold()) {
                        status = "LOW_STOCK";
                    } else {
                        status = "IN_STOCK";
                    }

                    return new MedicineAvailabilityItemDto(
                            inv.getId(),
                            inv.getMedicine().getId(),
                            inv.getMedicine().getName(),
                            inv.getMedicine().getGenericName(),
                            inv.getMedicine().getStrength(),
                            inv.getMedicine().getDosageForm(),
                            inv.getMedicine().getManufacturer(),
                            inv.getMedicine().getCategory() != null ? inv.getMedicine().getCategory().getName() : "General",
                            Boolean.TRUE.equals(inv.getMedicine().getRequiresPrescription()),
                            available,
                            inv.getQuantity(),
                            inv.getReservedQuantity(),
                            inv.getUnitPrice(),
                            inv.getBatchNo(),
                            inv.getExpiryDate() != null ? inv.getExpiryDate().toString() : "N/A",
                            isExpired,
                            isNearExpiry,
                            status,
                            inv.getDataSource(),
                            inv.getLastUpdatedAt() != null ? inv.getLastUpdatedAt().toString() : null
                    );
                })
                .sorted(Comparator.comparing(MedicineAvailabilityItemDto::medicineName))
                .collect(Collectors.toList());
    }

    /**
     * Pharmacy registration workflow for local medical stores.
     * Defaults to isApproved = false (pending admin verification).
     */
    public PharmacyDto registerPharmacy(RegisterPharmacyRequest req) {
        Pharmacy p = new Pharmacy();
        p.setName(req.name());
        p.setAddress(req.address());
        p.setLocality(req.locality());
        p.setCity(req.city() != null && !req.city().isBlank() ? req.city() : "Chennai");
        p.setPostalCode(req.postalCode());
        p.setContactPhone(req.contactPhone());
        p.setLatitude(req.latitude());
        p.setLongitude(req.longitude());
        p.setOpeningHours(req.openingHours() != null ? req.openingHours() : "08:00 AM - 10:00 PM");
        p.setDeliveryAvailable(req.deliveryAvailable() != null ? req.deliveryAvailable() : false);
        p.setLicenseNumber(req.licenseNumber());
        p.setWebsite(req.website());
        p.setEmail(req.email());
        p.setIsActive(true);
        // New self-registrations require administrative verification
        p.setIsApproved(false);
        p.setVerificationStatus("PENDING_APPROVAL");
        p.setDataSource("VERIFIED_DIRECTORY");
        p.setLastVerifiedAt(null);

        Pharmacy saved = pharmacyRepository.save(p);

        // Create or update settings
        PharmacySettings s = new PharmacySettings();
        s.setPharmacyId(saved.getId());
        s.setContactPhone(req.contactPhone());
        s.setDeliveryEnabled(req.deliveryAvailable() != null ? req.deliveryAvailable() : false);
        s.setDeliveryRadiusKm(req.deliveryRadiusKm() != null ? req.deliveryRadiusKm() : new BigDecimal("5.0"));
        s.setDeliveryFee(req.deliveryFee() != null ? req.deliveryFee() : new BigDecimal("30.00"));
        s.setOpens24x7(req.opens24x7() != null ? req.opens24x7() : false);
        s.setOpeningTime(LocalTime.of(8, 0));
        s.setClosingTime(LocalTime.of(22, 0));
        s.setAcceptsEmergencyOrders(true);
        settingsRepository.save(s);

        log.info("Pharmacy registered (Pending Verification): ID={}, Name={}", saved.getId(), saved.getName());
        return toDto(saved, null);
    }

    /**
     * Admin approves a registered pharmacy and emits PharmacyApprovedEvent.
     */
    public PharmacyDto approvePharmacy(Long pharmacyId) {
        Pharmacy p = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found with ID: " + pharmacyId));

        p.setIsApproved(true);
        p.setVerificationStatus("VERIFIED");
        p.setLastVerifiedAt(LocalDateTime.now());
        Pharmacy updated = pharmacyRepository.save(p);

        // Fire event to initialize any downstream cross-module listeners
        try {
            eventPublisher.publishEvent(new PharmacyApprovedEvent(pharmacyId, 1L));
        } catch (Exception e) {
            log.warn("Could not publish PharmacyApprovedEvent for {}: {}", pharmacyId, e.getMessage());
        }

        log.info("Pharmacy approved: ID={}, Name={}", updated.getId(), updated.getName());
        return toDto(updated, null);
    }

    /**
     * Admin rejects or requests corrections for a pharmacy registration.
     */
    public PharmacyDto rejectPharmacy(Long pharmacyId, String notes) {
        Pharmacy p = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found with ID: " + pharmacyId));

        p.setIsApproved(false);
        p.setVerificationStatus("REJECTED");
        p.setVerificationNotes(notes);
        Pharmacy updated = pharmacyRepository.save(p);
        log.info("Pharmacy rejected: ID={}, Reason={}", updated.getId(), notes);
        return toDto(updated, null);
    }

    @Transactional(readOnly = true)
    public List<PharmacyDto> getPendingPharmacies() {
        return pharmacyRepository.findByIsApprovedFalseAndIsActiveTrueOrderByCreatedAtDesc().stream()
                .map(p -> toDto(p, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctLocalities(String city) {
        String targetCity = (city != null && !city.isBlank()) ? city : "Chennai";
        return pharmacyRepository.findDistinctLocalitiesByCity(targetCity);
    }

    @Transactional(readOnly = true)
    public Page<PharmacyDto> searchPharmacies(String q, Pageable pageable) {
        return pharmacyRepository.searchPharmacies(q, pageable)
                .map(p -> toDto(p, null));
    }

    public double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 100.0) / 100.0;
    }

    public PharmacyDto toDto(Pharmacy p, Double distanceKm) {
        String mapsUrl = "https://www.google.com/maps/search/?api=1&query=" + p.getLatitude() + "," + p.getLongitude();

        // 1. Fetch settings for 24x7 and delivery
        Optional<PharmacySettings> settingsOpt = settingsRepository.findByPharmacyId(p.getId());
        boolean opens24x7 = false;
        Double deliveryRadius = null;
        Double deliveryFee = null;
        if (settingsOpt.isPresent()) {
            PharmacySettings s = settingsOpt.get();
            opens24x7 = Boolean.TRUE.equals(s.getOpens24x7());
            if (s.getDeliveryRadiusKm() != null) deliveryRadius = s.getDeliveryRadiusKm().doubleValue();
            if (s.getDeliveryFee() != null) deliveryFee = s.getDeliveryFee().doubleValue();
        } else if (p.getOpeningHours() != null && p.getOpeningHours().toLowerCase().contains("24x7")) {
            opens24x7 = true;
        }

        // 2. Fetch delivery status and ETA
        String deliveryStatus = "DELIVERY_UNAVAILABLE";
        if (deliveryQuery != null) {
            try {
                DeliveryStatus ds = deliveryQuery.statusFor(p.getId());
                if (ds != null) {
                    deliveryStatus = ds.name();
                }
            } catch (Exception ignored) {
                deliveryStatus = "DELIVERY_UNAVAILABLE";
            }
        }
        Integer deliveryETA = null;
        if (distanceKm != null && !"DELIVERY_UNAVAILABLE".equals(deliveryStatus) && !"UNAVAILABLE".equals(deliveryStatus)) {
            deliveryETA = Math.min(120, Math.max(20, 20 + (int) Math.round(distanceKm * 4.0)));
        }

        // 3. Inspect real inventory records to evaluate stockStatus and availableCount
        List<PharmacyInventory> inventoryList = inventoryRepository.findByPharmacyIdAndIsActiveTrue(p.getId());
        LocalDate today = LocalDate.now();

        String stockStatus;
        int availableMedicinesCount = 0;
        String inventoryNotice;

        if (inventoryList.isEmpty()) {
            stockStatus = "STOCK_NOT_VERIFIED";
            inventoryNotice = "Stock not verified. Call pharmacy to confirm availability.";
        } else {
            boolean hasLive = false;
            boolean hasSample = false;
            int inStockCount = 0;

            for (PharmacyInventory inv : inventoryList) {
                boolean expired = inv.getExpiryDate() != null && inv.getExpiryDate().isBefore(today);
                if (!expired && inv.getAvailableQuantity() > 0) {
                    inStockCount++;
                    if ("LIVE".equalsIgnoreCase(inv.getDataSource())) {
                        hasLive = true;
                    } else {
                        hasSample = true;
                    }
                }
            }

            availableMedicinesCount = inStockCount;

            if (inStockCount == 0) {
                stockStatus = "OUT_OF_STOCK";
                inventoryNotice = "Currently out of emergency stock.";
            } else if (hasLive) {
                stockStatus = "VERIFIED_LIVE";
                inventoryNotice = "Live verified stock synchronized from pharmacy system.";
            } else {
                stockStatus = "SAMPLE_DEMO";
                inventoryNotice = "Sample demo data for demonstration.";
            }
        }

        return new PharmacyDto(
                p.getId(),
                p.getName(),
                p.getAddress(),
                p.getLocality(),
                p.getCity(),
                p.getPostalCode(),
                p.getContactPhone(),
                p.getLatitude() != null ? BigDecimal.valueOf(p.getLatitude()) : null,
                p.getLongitude() != null ? BigDecimal.valueOf(p.getLongitude()) : null,
                distanceKm,
                p.getOpeningHours(),
                p.getIsActive(),
                p.getIsApproved(),
                p.getDeliveryAvailable(),
                p.getDataSource(),
                p.getLastVerifiedAt() != null ? p.getLastVerifiedAt().toString() : null,
                mapsUrl,
                p.getLicenseNumber(),
                p.getWebsite(),
                p.getEmail(),
                p.getVerificationStatus(),
                opens24x7,
                deliveryRadius,
                deliveryFee,
                deliveryStatus,
                deliveryETA,
                stockStatus,
                availableMedicinesCount,
                inventoryNotice,
                p.getProviderPlaceId(),
                p.getFacilityType() != null ? p.getFacilityType() : "PHARMACY"
        );
    }
}
