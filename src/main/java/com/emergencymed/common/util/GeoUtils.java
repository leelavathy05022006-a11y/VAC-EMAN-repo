package com.emergencymed.common.util;

public final class GeoUtils {
    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {}

    /**
     * Calculates the great-circle distance between two geographic coordinates using the Haversine formula.
     * @param lat1 Latitude of point 1 in decimal degrees
     * @param lon1 Longitude of point 1 in decimal degrees
     * @param lat2 Latitude of point 2 in decimal degrees
     * @param lon2 Longitude of point 2 in decimal degrees
     * @return Distance in kilometers, rounded to 2 decimal places
     */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(rLat1) * Math.cos(rLat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;
        return Math.round(distance * 100.0) / 100.0;
    }

    /**
     * Checks if coordinates fall within bounding box for index query optimization.
     */
    public static double[] boundingBox(double lat, double lon, double radiusKm) {
        double dLat = radiusKm / EARTH_RADIUS_KM;
        double minLat = lat - Math.toDegrees(dLat);
        double maxLat = lat + Math.toDegrees(dLat);

        double dLon = radiusKm / (EARTH_RADIUS_KM * Math.cos(Math.toRadians(lat)));
        double minLon = lon - Math.toDegrees(dLon);
        double maxLon = lon + Math.toDegrees(dLon);

        return new double[]{minLat, maxLat, minLon, maxLon};
    }
}
