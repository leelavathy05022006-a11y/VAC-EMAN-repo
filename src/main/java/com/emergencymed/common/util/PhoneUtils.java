package com.emergencymed.common.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class PhoneUtils {
    public static final String PHONE_REGEX = "^[6-9][0-9]{9}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    private PhoneUtils() {}

    public static boolean isValid(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static String clean(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 12 && digits.startsWith("91")) {
            digits = digits.substring(2);
        }
        return digits;
    }

    public static String format(String phone) {
        if (phone == null) return "";
        String cleaned = clean(phone);
        if (cleaned.length() == 10) {
            return "+91 " + cleaned.substring(0, 5) + " " + cleaned.substring(5);
        }
        return phone;
    }

    public static String mask(String phone) {
        if (phone == null) return "";
        String cleaned = clean(phone);
        if (cleaned.length() == 10) {
            return "+91 ******" + cleaned.substring(6);
        }
        return phone;
    }

    public static String formatTelUrl(String phone) {
        if (phone == null || phone.isBlank()) return "tel:";
        String cleaned = clean(phone);
        return "tel:+91" + cleaned;
    }

    public static String formatWhatsappUrl(String phone, String text) {
        if (phone == null || phone.isBlank()) return "#";
        String cleaned = clean(phone);
        String url = "https://wa.me/91" + cleaned;
        if (text != null && !text.isBlank()) {
            try {
                url += "?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
            } catch (Exception ignored) {}
        }
        return url;
    }

    public static String formatWhatsappUrl(String phone, String medicineName, Integer qty) {
        String text = null;
        if (medicineName != null && !medicineName.isBlank()) {
            text = "Hello, do you have " + medicineName + (qty != null ? " (Qty: " + qty + ")" : "") + " available in stock for emergency delivery?";
        }
        return formatWhatsappUrl(phone, text);
    }
}
