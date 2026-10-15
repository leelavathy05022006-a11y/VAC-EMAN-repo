package com.emergencymed.chatbot.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ChatbotSanitizer {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern CONTROL_CHARS_PATTERN = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");

    // Phone numbers: Indian 10-digit or with +91/91 prefix
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?91[\\s-]?)?[6-9]\\d{9}");

    // Emergency order reference codes: e.g. REQ-2026-001, REQ-101
    private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("(?i)REQ-\\d+(?:-\\d+)?");

    /**
     * Cleans raw user input: strips HTML tags, control characters, and normalizes whitespace.
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        String stripped = HTML_TAG_PATTERN.matcher(input).replaceAll("");
        stripped = CONTROL_CHARS_PATTERN.matcher(stripped).replaceAll("");
        return stripped.trim();
    }

    /**
     * Masks sensitive details (phone numbers and order reference codes) prior to database persistence.
     */
    public String maskForStorage(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        // Mask phone numbers: e.g., 9000000001 -> 90XXXXXX01
        String masked = PHONE_PATTERN.matcher(text).replaceAll(mr -> {
            String p = mr.group();
            if (p.length() >= 10) {
                return p.substring(0, 2) + "******" + p.substring(p.length() - 2);
            }
            return "[PHONE_MASKED]";
        });

        // Mask order codes: e.g., REQ-2026-001 -> REQ-****-001
        masked = ORDER_CODE_PATTERN.matcher(masked).replaceAll(mr -> {
            String code = mr.group();
            int dashIndex = code.lastIndexOf('-');
            if (dashIndex > 0) {
                return "REQ-****-" + code.substring(dashIndex + 1);
            }
            return "REQ-****";
        });

        return masked;
    }
}
