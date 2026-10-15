package com.emergencymed.common.util;

import java.util.Locale;

public final class MapLinks {
    private MapLinks() {}

    /**
     * Generates a Google Maps directions URL to navigate to the specified destination coordinates.
     */
    public static String directionsUrl(double lat, double lng) {
        return String.format(Locale.US, "https://www.google.com/maps/dir/?api=1&destination=%.6f,%.6f", lat, lng);
    }

    /**
     * Generates a Google Maps directions URL with origin, destination, and travel mode.
     */
    public static String buildDirectionsUrl(double originLat, double originLng, double destLat, double destLng, String travelMode) {
        String mode = travelMode != null && !travelMode.isBlank() ? travelMode.toLowerCase() : "driving";
        return String.format(Locale.US, "https://www.google.com/maps/dir/?api=1&origin=%.6f,%.6f&destination=%.6f,%.6f&travelmode=%s",
                originLat, originLng, destLat, destLng, mode);
    }

    /**
     * Generates a Google Maps place/marker URL for display.
     */
    public static String markerUrl(double lat, double lng) {
        return String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%.6f,%.6f", lat, lng);
    }
}
