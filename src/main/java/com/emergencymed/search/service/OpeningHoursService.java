package com.emergencymed.search.service;

import com.emergencymed.common.dto.PharmacyDto;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OpeningHoursService {

    public static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Determines whether a pharmacy is currently open based on Asia/Kolkata time.
     * Handles 24x7 pharmacies and overnight hours crossing midnight (e.g., 22:00 to 06:00).
     */
    public boolean isOpenNow(PharmacyDto pharmacy) {
        if (pharmacy == null) {
            return false;
        }
        if (pharmacy.isOpens24x7()) {
            return true;
        }

        String openStr = pharmacy.getOpeningTime();
        String closeStr = pharmacy.getClosingTime();

        if (openStr == null || closeStr == null || openStr.isBlank() || closeStr.isBlank()) {
            return true; // Default to open if not specified
        }

        LocalTime currentTime = ZonedDateTime.now(IST_ZONE).toLocalTime();
        return isTimeWithinHours(currentTime, openStr, closeStr);
    }

    /**
     * Evaluates whether a given time falls within the opening and closing hours.
     */
    public boolean isTimeWithinHours(LocalTime time, String openStr, String closeStr) {
        try {
            LocalTime open = LocalTime.parse(openStr.trim(), TIME_FORMATTER);
            LocalTime close = LocalTime.parse(closeStr.trim(), TIME_FORMATTER);

            if (open.equals(close)) {
                return true; // 24 hours
            }

            if (open.isBefore(close)) {
                // Same-day operating hours (e.g., 08:00 to 22:00)
                return !time.isBefore(open) && !time.isAfter(close);
            } else {
                // Overnight operating hours (e.g., 22:00 to 06:00)
                return !time.isBefore(open) || !time.isAfter(close);
            }
        } catch (Exception e) {
            return true;
        }
    }
}
