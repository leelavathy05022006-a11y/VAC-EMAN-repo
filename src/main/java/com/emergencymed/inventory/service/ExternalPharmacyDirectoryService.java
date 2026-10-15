package com.emergencymed.inventory.service;

import com.emergencymed.inventory.dto.GeocodeSuggestionDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExternalPharmacyDirectoryService {
    private static final Logger log = LoggerFactory.getLogger(ExternalPharmacyDirectoryService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${google.maps.api-key:}")
    private String googleMapsApiKey;

    // Cache for reverse geocoding and external suggestions (TTL ~ 1 hour)
    private final Map<String, CacheEntry<String>> reverseGeocodeCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<List<GeocodeSuggestionDto>>> suggestionCache = new ConcurrentHashMap<>();

    // Curated offline-resilient Chennai localities, streets, landmarks & PIN codes
    private static final List<GeocodeSuggestionDto> CURATED_CHENNAI_LOCALITIES = List.of(
            new GeocodeSuggestionDto("Poonamallee", "Poonamallee", "Chennai", "600056", 13.0489, 80.1132, "Poonamallee Trunk Road, Chennai, Tamil Nadu 600056"),
            new GeocodeSuggestionDto("Poonamallee High Road", "Poonamallee", "Chennai", "600056", 13.0535, 80.1190, "Poonamallee High Road, Chennai, Tamil Nadu 600056"),
            new GeocodeSuggestionDto("Kumananchavadi", "Kumananchavadi", "Chennai", "600056", 13.0510, 80.1310, "Kumananchavadi Junction, Poonamallee, Chennai 600056"),
            new GeocodeSuggestionDto("Mangadu", "Mangadu", "Chennai", "600122", 13.0425, 80.1205, "Mangadu High Road, Chennai, Tamil Nadu 600122"),
            new GeocodeSuggestionDto("Iyyappanthangal", "Iyyappanthangal", "Chennai", "600056", 13.0390, 80.1415, "Mount Poonamallee Road, Iyyappanthangal, Chennai 600056"),
            new GeocodeSuggestionDto("Porur", "Porur", "Chennai", "600116", 13.0382, 80.1565, "Porur Roundtana / Trunk Road, Chennai 600116"),
            new GeocodeSuggestionDto("Karambakkam", "Porur", "Chennai", "600116", 13.0350, 80.1510, "Karambakkam, Porur, Chennai 600116"),
            new GeocodeSuggestionDto("Avadi", "Avadi", "Chennai", "600054", 13.1147, 80.1018, "Nehru Memorial Road, Avadi, Chennai 600054"),
            new GeocodeSuggestionDto("Avadi Checkpost", "Avadi", "Chennai", "600054", 13.1180, 80.1055, "Poonamallee-Avadi High Road, Avadi Checkpost 600054"),
            new GeocodeSuggestionDto("Thirumazhisai", "Thirumazhisai", "Chennai", "600124", 13.0545, 80.0630, "Bangalore National Highway, Thirumazhisai 600124"),
            new GeocodeSuggestionDto("Nazarathpet", "Nazarathpet", "Chennai", "600123", 13.0512, 80.0925, "NH4 Service Road, Nazarathpet 600123"),
            new GeocodeSuggestionDto("Kundrathur", "Kundrathur", "Chennai", "600069", 13.0035, 80.0965, "Murugan Temple Road, Kundrathur, Chennai 600069"),
            new GeocodeSuggestionDto("Ambattur", "Ambattur", "Chennai", "600053", 13.1143, 80.1548, "MTH Road, Ambattur OT, Chennai 600053"),
            new GeocodeSuggestionDto("Ambattur Industrial Estate", "Ambattur", "Chennai", "600058", 13.0880, 80.1610, "Industrial Estate 3rd Main Rd, Ambattur 600058"),
            new GeocodeSuggestionDto("Tambaram", "Tambaram", "Chennai", "600045", 12.9249, 80.1000, "GST Road, West Tambaram, Chennai 600045"),
            new GeocodeSuggestionDto("Chromepet", "Chromepet", "Chennai", "600044", 12.9516, 80.1402, "Radha Nagar Main Road, Chromepet, Chennai 600044"),
            new GeocodeSuggestionDto("Pallavaram", "Pallavaram", "Chennai", "600043", 12.9675, 80.1491, "GST Road, Pallavaram, Chennai 600043"),
            new GeocodeSuggestionDto("Guindy", "Guindy", "Chennai", "600032", 13.0067, 80.2020, "Mount Road / Race Course, Guindy, Chennai 600032"),
            new GeocodeSuggestionDto("Anna Nagar", "Anna Nagar", "Chennai", "600040", 13.0850, 80.2101, "2nd Avenue / Roundtana, Anna Nagar, Chennai 600040"),
            new GeocodeSuggestionDto("T. Nagar", "T. Nagar", "Chennai", "600017", 13.0418, 80.2341, "Pondy Bazaar / Usman Road, T. Nagar, Chennai 600017"),
            new GeocodeSuggestionDto("Velachery", "Velachery", "Chennai", "600042", 12.9781, 80.2206, "100 Feet Bypass Road, Velachery, Chennai 600042"),
            new GeocodeSuggestionDto("Adyar", "Adyar", "Chennai", "600020", 13.0012, 80.2565, "Lattice Bridge Road, Adyar, Chennai 600020"),
            new GeocodeSuggestionDto("Mylapore", "Mylapore", "Chennai", "600004", 13.0339, 80.2676, "Luz Church Road, Mylapore, Chennai 600004"),
            new GeocodeSuggestionDto("Nungambakkam", "Nungambakkam", "Chennai", "600034", 13.0604, 80.2407, "Nungambakkam High Road, Chennai 600034"),
            new GeocodeSuggestionDto("Maduravoyal", "Maduravoyal", "Chennai", "600095", 13.0632, 80.1652, "Alapakkam Main Road, Maduravoyal, Chennai 600095"),
            new GeocodeSuggestionDto("Valasaravakkam", "Valasaravakkam", "Chennai", "600087", 13.0402, 80.1738, "Arcot Road, Valasaravakkam, Chennai 600087"),
            new GeocodeSuggestionDto("Koyambedu", "Koyambedu", "Chennai", "600107", 13.0694, 80.1948, "CMBT / Wholesale Market, Koyambedu 600107"),
            new GeocodeSuggestionDto("Vadapalani", "Vadapalani", "Chennai", "600026", 13.0500, 80.2120, "Arcot Road, Vadapalani, Chennai 600026"),
            new GeocodeSuggestionDto("Saidapet", "Saidapet", "Chennai", "600015", 13.0210, 80.2230, "Anna Salai, Saidapet, Chennai 600015"),
            new GeocodeSuggestionDto("Thiruvanmiyur", "Thiruvanmiyur", "Chennai", "600041", 12.9830, 80.2590, "ECR Junction, Thiruvanmiyur, Chennai 600041"),
            new GeocodeSuggestionDto("Sholinganallur", "Sholinganallur", "Chennai", "600119", 12.9010, 80.2279, "OMR IT Corridor Junction, Sholinganallur 600119")
    );

    public ExternalPharmacyDirectoryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /**
     * Search locality, street, landmark or PIN code suggestions.
     * Combines curated instant dataset with OpenStreetMap Nominatim lookup.
     */
    public List<GeocodeSuggestionDto> searchLocalitySuggestions(String query) {
        if (query == null || query.trim().length() < 2) {
            return CURATED_CHENNAI_LOCALITIES.subList(0, Math.min(8, CURATED_CHENNAI_LOCALITIES.size()));
        }

        String qClean = query.trim().toLowerCase();

        // 1. Check cache
        CacheEntry<List<GeocodeSuggestionDto>> cached = suggestionCache.get(qClean);
        if (cached != null && !cached.isExpired()) {
            return cached.data;
        }

        List<GeocodeSuggestionDto> results = new ArrayList<>();

        // 2. Search curated preloaded list first
        for (GeocodeSuggestionDto item : CURATED_CHENNAI_LOCALITIES) {
            if (item.name().toLowerCase().contains(qClean) ||
                item.locality().toLowerCase().contains(qClean) ||
                (item.postalCode() != null && item.postalCode().contains(qClean)) ||
                item.formattedAddress().toLowerCase().contains(qClean)) {
                results.add(item);
            }
        }

        // 3. If online search available and query has >= 3 chars, query Nominatim
        if (qClean.length() >= 3) {
            try {
                String encodedQ = URLEncoder.encode(query.trim() + ", Chennai", StandardCharsets.UTF_8);
                String nominatimUrl = "https://nominatim.openstreetmap.org/search?q=" + encodedQ +
                        "&format=json&addressdetails=1&limit=6&countrycodes=in";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(nominatimUrl))
                        .header("User-Agent", "EmergencyMed-Chennai-Directory/1.0 (contact: support@emergencymed.org)")
                        .timeout(Duration.ofSeconds(2))
                        .GET()
                        .build();

                HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    JsonNode array = objectMapper.readTree(resp.body());
                    if (array.isArray()) {
                        for (JsonNode item : array) {
                            String displayName = item.path("display_name").asText();
                            double lat = item.path("lat").asDouble();
                            double lon = item.path("lon").asDouble();

                            JsonNode addr = item.path("address");
                            String suburb = addr.has("suburb") ? addr.path("suburb").asText() :
                                    (addr.has("neighbourhood") ? addr.path("neighbourhood").asText() :
                                            (addr.has("road") ? addr.path("road").asText() : query));
                            String postcode = addr.has("postcode") ? addr.path("postcode").asText() : "";
                            String city = addr.has("city") ? addr.path("city").asText() : "Chennai";

                            // Avoid duplicates in results
                            boolean duplicate = results.stream().anyMatch(r ->
                                    Math.abs(r.latitude() - lat) < 0.005 && Math.abs(r.longitude() - lon) < 0.005);

                            if (!duplicate) {
                                results.add(new GeocodeSuggestionDto(
                                        suburb, suburb, city, postcode, lat, lon, ReviewAddress(displayName, suburb)
                                ));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Online geocoding lookup bypassed: {}", e.getMessage());
            }
        }

        // Deduplicate and cap to 10
        List<GeocodeSuggestionDto> finalResults = results.stream().limit(10).toList();
        suggestionCache.put(qClean, new CacheEntry<>(finalResults, 3600_000));
        return finalResults;
    }

    /**
     * Reverse geocodes coordinates to display approximate locality and address.
     */
    public String reverseGeocode(double lat, double lng) {
        String cacheKey = String.format(Locale.US, "%.3f,%.3f", lat, lng);
        CacheEntry<String> cached = reverseGeocodeCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.data;
        }

        // Check nearest from curated list
        GeocodeSuggestionDto nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (GeocodeSuggestionDto item : CURATED_CHENNAI_LOCALITIES) {
            double d = Math.hypot(item.latitude() - lat, item.longitude() - lng);
            if (d < minDistance) {
                minDistance = d;
                nearest = item;
            }
        }

        String defaultResolved = nearest != null ?
                nearest.locality() + ", Chennai (" + nearest.postalCode() + ")" :
                "Chennai, Tamil Nadu";

        // Try OpenStreetMap Nominatim reverse geocoding with timeout
        try {
            String url = String.format(Locale.US,
                    "https://nominatim.openstreetmap.org/reverse?lat=%.6f&lon=%.6f&format=json&addressdetails=1",
                    lat, lng);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "EmergencyMed-Chennai-Directory/1.0 (contact: support@emergencymed.org)")
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(resp.body());
                JsonNode addr = root.path("address");
                String road = addr.path("road").asText("");
                String suburb = addr.path("suburb").asText(
                        addr.path("neighbourhood").asText(addr.path("residential").asText("")));
                String postcode = addr.path("postcode").asText("");

                StringBuilder sb = new StringBuilder();
                if (!road.isBlank()) sb.append(road).append(", ");
                if (!suburb.isBlank()) sb.append(suburb).append(", ");
                sb.append("Chennai");
                if (!postcode.isBlank()) sb.append(" - ").append(postcode);

                String formatted = sb.toString();
                reverseGeocodeCache.put(cacheKey, new CacheEntry<>(formatted, 3600_000));
                return formatted;
            }
        } catch (Exception e) {
            log.debug("Online reverse geocode bypassed: {}", e.getMessage());
        }

        reverseGeocodeCache.put(cacheKey, new CacheEntry<>(defaultResolved, 3600_000));
        return defaultResolved;
    }

    private String ReviewAddress(String fullAddress, String fallback) {
        if (fullAddress == null || fullAddress.isBlank()) return fallback;
        if (fullAddress.length() > 80) {
            return fullAddress.substring(0, 77) + "...";
        }
        return fullAddress;
    }

    private static class CacheEntry<T> {
        final T data;
        final long expiresAt;

        CacheEntry(T data, long ttlMillis) {
            this.data = data;
            this.expiresAt = System.currentTimeMillis() + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
