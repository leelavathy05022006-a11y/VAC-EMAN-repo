package com.emergencymed.search.service;

import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.common.dto.PharmacyDto;
import com.emergencymed.common.exception.ResourceNotFoundException;
import com.emergencymed.common.exception.ValidationException;
import com.emergencymed.common.util.MapLinks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NavigationService {

    private final PharmacyLookup pharmacyLookup;
    private final ContactLinkService contactLinkService;

    @Autowired
    public NavigationService(PharmacyLookup pharmacyLookup, ContactLinkService contactLinkService) {
        this.pharmacyLookup = pharmacyLookup;
        this.contactLinkService = contactLinkService;
    }

    /**
     * Builds Google Maps Directions URL from user coordinates to pharmacy and logs the NAVIGATION action.
     */
    public Map<String, String> getDirections(Long pharmacyId, Double lat, Double lng, String mode) {
        if (lat == null || lng == null) {
            throw new ValidationException("Location is required");
        }

        PharmacyDto pharmacy = pharmacyLookup.findApproved(pharmacyId).orElse(null);
        if (pharmacy == null) {
            throw new ResourceNotFoundException("Approved pharmacy not found for ID: " + pharmacyId);
        }

        String directionsUrl = MapLinks.buildDirectionsUrl(
                lat, lng, pharmacy.getLatitude(), pharmacy.getLongitude(), mode
        );

        // Log NAVIGATION action
        contactLinkService.logContact(pharmacyId, null, "NAVIGATION");

        Map<String, String> result = new HashMap<>();
        result.put("directionsUrl", directionsUrl);
        result.put("pharmacyName", pharmacy.getName());
        result.put("destinationLat", String.valueOf(pharmacy.getLatitude()));
        result.put("destinationLng", String.valueOf(pharmacy.getLongitude()));
        return result;
    }
}
