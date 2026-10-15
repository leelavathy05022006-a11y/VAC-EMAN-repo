package com.emergencymed.search;

import com.emergencymed.common.util.GeoUtils;
import com.emergencymed.common.util.MapLinks;
import com.emergencymed.common.util.PhoneUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MedicineSearchTest {

    @Test
    @DisplayName("Haversine formula calculates accurate distance between Chennai coordinates")
    void testHaversineDistance() {
        // Apollo Pharmacy T. Nagar to Apollo Greams Road, Chennai (~3.5 km)
        double lat1 = 13.0418;
        double lng1 = 80.2341;
        double lat2 = 13.0583;
        double lng2 = 80.2508;

        double distance = GeoUtils.haversineKm(lat1, lng1, lat2, lng2);
        assertTrue(distance > 2.0 && distance < 4.0, "Expected distance around 2.5-3.5 km, got: " + distance);

        // Same point distance must be 0.0
        assertEquals(0.0, GeoUtils.haversineKm(lat1, lng1, lat1, lng1));
    }

    @Test
    @DisplayName("MapLinks generates valid Google Maps driving directions URL with origin and destination")
    void testMapLinksDirectionsUrl() {
        double originLat = 13.0050;
        double originLng = 80.2550;
        double destLat = 13.0033;
        double destLng = 80.2550;

        String url = MapLinks.buildDirectionsUrl(originLat, originLng, destLat, destLng, "driving");
        assertNotNull(url);
        assertTrue(url.startsWith("https://www.google.com/maps/dir/"));
        assertTrue(url.contains("origin=13.005000,80.255000"));
        assertTrue(url.contains("destination=13.003300,80.255000"));
        assertTrue(url.contains("travelmode=driving"));
    }

    @Test
    @DisplayName("PhoneUtils formats telephone and WhatsApp contact links correctly")
    void testPhoneUtilsUrls() {
        String phone = "9840123456";
        String telUrl = PhoneUtils.formatTelUrl(phone);
        assertNotNull(telUrl);
        assertTrue(telUrl.startsWith("tel:"));
        assertTrue(telUrl.contains("9840123456"));

        String waUrl = PhoneUtils.formatWhatsappUrl(phone, "Adrenaline Injection", 2);
        assertNotNull(waUrl);
        assertTrue(waUrl.startsWith("https://wa.me/"));
        assertTrue(waUrl.contains("Adrenaline"));
    }
}
