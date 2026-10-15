package com.emergencymed.chatbot.service;

import com.emergencymed.chatbot.dto.ChatIntent;
import com.emergencymed.chatbot.dto.ExtractedQueryEntities;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RuleBasedIntentHandler {

    // Locality coordinates mapping for Chennai
    private static final Map<String, double[]> LOCALITY_COORDINATES = new LinkedHashMap<>();

    static {
        LOCALITY_COORDINATES.put("adyar", new double[]{13.0035, 80.2570});
        LOCALITY_COORDINATES.put("guindy", new double[]{13.0100, 80.2200});
        LOCALITY_COORDINATES.put("t nagar", new double[]{13.0418, 80.2341});
        LOCALITY_COORDINATES.put("t. nagar", new double[]{13.0418, 80.2341});
        LOCALITY_COORDINATES.put("t.nagar", new double[]{13.0418, 80.2341});
        LOCALITY_COORDINATES.put("tnagar", new double[]{13.0418, 80.2341});
        LOCALITY_COORDINATES.put("tambaram", new double[]{12.9249, 80.1000});
        LOCALITY_COORDINATES.put("besant nagar", new double[]{13.0005, 80.2660});
        LOCALITY_COORDINATES.put("velachery", new double[]{12.9815, 80.2180});
        LOCALITY_COORDINATES.put("anna nagar", new double[]{13.0850, 80.2101});
        LOCALITY_COORDINATES.put("porur", new double[]{13.0382, 80.1565});
        LOCALITY_COORDINATES.put("poonamallee", new double[]{13.0489, 80.1132});
        LOCALITY_COORDINATES.put("kumananchavadi", new double[]{13.0510, 80.1310});
        LOCALITY_COORDINATES.put("iyyappanthangal", new double[]{13.0390, 80.1415});
        LOCALITY_COORDINATES.put("mangadu", new double[]{13.0425, 80.1205});
        LOCALITY_COORDINATES.put("ambattur", new double[]{13.1143, 80.1548});
        LOCALITY_COORDINATES.put("avadi", new double[]{13.1147, 80.1018});
        LOCALITY_COORDINATES.put("pallavaram", new double[]{12.9675, 80.1491});
        LOCALITY_COORDINATES.put("chromepet", new double[]{12.9516, 80.1402});
        LOCALITY_COORDINATES.put("mylapore", new double[]{13.0368, 80.2676});
        LOCALITY_COORDINATES.put("nungambakkam", new double[]{13.0569, 80.2425});
        LOCALITY_COORDINATES.put("chennai", new double[]{13.0827, 80.2707});
    }

    // Emergency patterns
    private static final Pattern EMERGENCY_PATTERN = Pattern.compile(
            "(?i)\\b(chest pain|heart attack|trouble breathing|difficulty breathing|shortness of breath|breathlessness|unconscious|unconsciousness|fainting|severe bleeding|heavy bleeding|poison|poisoning|suicid|stroke|cardiac arrest|snake bite|asthma attack|overdose|seizure|convulsion|choking|avacharam|marana|uyirukku aabathu)\\b"
    );

    // Order code pattern (e.g. EMR-20261014-0001 or REQ-101)
    private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("(?i)\\b((?:EMR|REQ)-\\d+(?:-\\d+)?)\\b");

    // Quantity patterns
    private static final Pattern EXPLICIT_QTY_PATTERN = Pattern.compile("(?i)\\b(?:qty|quantity|count)\\s*[:=]?\\s*(\\d{1,3})\\b");
    private static final Pattern UNIT_QTY_PATTERN = Pattern.compile("(?i)\\b(\\d{1,3})\\s*(?:units?|tablets?|capsules?|strips?|bottles?|boxes?|vials?|nos?|pieces?)\\b");

    // Phone number pattern
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?i)\\b([6-9]\\d{9})\\b");

    // Known Brand to Generic mappings
    private static final Map<String, String> BRAND_TO_GENERIC = new LinkedHashMap<>();
    static {
        BRAND_TO_GENERIC.put("dolo 650", "Paracetamol 650mg");
        BRAND_TO_GENERIC.put("dolo 500", "Paracetamol 500mg");
        BRAND_TO_GENERIC.put("dolo", "Paracetamol 650mg");
        BRAND_TO_GENERIC.put("calpol", "Paracetamol 500mg");
        BRAND_TO_GENERIC.put("crocin", "Paracetamol 650mg");
        BRAND_TO_GENERIC.put("pacimol", "Paracetamol 650mg");
        BRAND_TO_GENERIC.put("volini", "Volini Ointment");
        BRAND_TO_GENERIC.put("moov", "Moov Pain Relief");
        BRAND_TO_GENERIC.put("iodex", "Iodex Balm");
        BRAND_TO_GENERIC.put("omnigel", "Omnigel");
        BRAND_TO_GENERIC.put("fastum", "Pain Relief Gel");
        BRAND_TO_GENERIC.put("omez", "Omeprazole 20mg");
        BRAND_TO_GENERIC.put("pan 40", "Pantoprazole 40mg");
        BRAND_TO_GENERIC.put("pantocid", "Pantoprazole 40mg");
        BRAND_TO_GENERIC.put("electral", "ORS Packets");
        BRAND_TO_GENERIC.put("limcee", "Vitamin C Tablets 500mg");
        BRAND_TO_GENERIC.put("celin", "Vitamin C Tablets 500mg");
        BRAND_TO_GENERIC.put("augmentin", "Amoxicillin 500mg");
        BRAND_TO_GENERIC.put("novamox", "Amoxicillin 500mg");
        BRAND_TO_GENERIC.put("azithral", "Azithromycin 250mg");
        BRAND_TO_GENERIC.put("azee", "Azithromycin 250mg");
        BRAND_TO_GENERIC.put("glycomet", "Metformin 500mg");
        BRAND_TO_GENERIC.put("amaryl", "Glimepiride 2mg");
        BRAND_TO_GENERIC.put("amlong", "Amlodipine 5mg");
        BRAND_TO_GENERIC.put("atorva", "Atorvastatin 10mg");
        BRAND_TO_GENERIC.put("brufen", "Ibuprofen 400mg");
        BRAND_TO_GENERIC.put("combiflam", "Ibuprofen 400mg");
        BRAND_TO_GENERIC.put("montair", "Montelukast 10mg");
        BRAND_TO_GENERIC.put("zinconia", "Zinc Sulfate 50mg");
        BRAND_TO_GENERIC.put("honitus", "Dextromethorphan Syrup 100ml");
        BRAND_TO_GENERIC.put("lantus", "Insulin Glargine");
    }

    // Known generic catalogue medicines
    private static final List<String> KNOWN_GENERIC_MEDICINES = List.of(
            "paracetamol 650mg", "paracetamol 650", "paracetamol 500mg", "paracetamol 500", "paracetamol",
            "cetirizine 10mg", "cetirizine", "ors packets", "ors", "vitamin c tablets 500mg", "vitamin c",
            "omeprazole 20mg", "omeprazole", "pantoprazole 40mg", "pantoprazole",
            "amoxicillin 500mg", "amoxicillin", "azithromycin 250mg", "azithromycin",
            "metformin 500mg", "metformin", "glimepiride 2mg", "glimepiride",
            "amlodipine 5mg", "amlodipine", "atorvastatin 10mg", "atorvastatin",
            "ibuprofen 400mg", "ibuprofen", "montelukast 10mg", "montelukast",
            "zinc sulfate 50mg", "zinc sulfate", "dextromethorphan syrup", "dextromethorphan",
            "insulin glargine", "insulin"
    );

    /**
     * Analyzes raw query text, extracts intent, entities, locality, quantity, symptoms, and detects emergencies.
     */
    public ExtractedQueryEntities analyzeQuery(String text, Double clientLat, Double clientLng, String clientLocality) {
        ExtractedQueryEntities entities = new ExtractedQueryEntities();
        if (text == null || text.isBlank()) {
            entities.setIntent(ChatIntent.GREETING);
            return entities;
        }

        String lower = text.toLowerCase().trim();

        // 1. First priority: Check for severe life-threatening emergencies
        if (EMERGENCY_PATTERN.matcher(lower).find()) {
            entities.setEmergency(true);
            entities.setIntent(ChatIntent.EMERGENCY);
            entities.setEmergencyDetails("Critical condition keywords detected");
            return entities;
        }

        // 2. Extract Locality & Coordinates
        extractLocalityAndCoordinates(entities, lower, clientLat, clientLng, clientLocality);

        // 3. Extract Order Reference Code
        Matcher orderMatcher = ORDER_CODE_PATTERN.matcher(text);
        if (orderMatcher.find()) {
            entities.setOrderReference(orderMatcher.group(1).toUpperCase());
        }

        // 4. Extract Phone Number
        Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
            entities.setCustomerPhone(phoneMatcher.group(1));
        }

        // 5. Extract Quantity
        extractQuantity(entities, lower);

        // 6. Extract Brand / Medicine Name
        extractMedicineOrBrand(entities, lower);

        // 7. Extract Symptom
        extractSymptom(entities, lower);

        // 8. Check confirmation or cancellation
        checkConfirmationOrCancellation(entities, lower);

        // 9. Determine Intent
        entities.setIntent(detectIntent(lower, entities));

        return entities;
    }

    private void extractMedicineOrBrand(ExtractedQueryEntities entities, String lower) {
        // Check Brand names first
        for (Map.Entry<String, String> entry : BRAND_TO_GENERIC.entrySet()) {
            Pattern p = Pattern.compile("(?i)\\b" + Pattern.quote(entry.getKey()) + "\\b");
            if (p.matcher(lower).find()) {
                entities.setBrandName(entry.getKey());
                entities.setMedicineName(entry.getValue());
                return;
            }
        }

        // Check Known Generics
        for (String med : KNOWN_GENERIC_MEDICINES) {
            Pattern p = Pattern.compile("(?i)\\b" + Pattern.quote(med) + "\\b");
            if (p.matcher(lower).find()) {
                entities.setMedicineName(med);
                return;
            }
        }

        // Regex attempt for "marunthu", "medicine <name>", "tablet <name>"
        Matcher medMatcher = Pattern.compile("(?i)(?:medicine|tablet|syrup|capsule|marunthu|marundhu)\\s+([a-zA-Z0-9]+)").matcher(lower);
        if (medMatcher.find()) {
            String candidate = medMatcher.group(1).trim();
            if (!isStopWord(candidate)) {
                entities.setMedicineName(candidate);
            }
        }
    }

    private void extractSymptom(ExtractedQueryEntities entities, String lower) {
        if (lower.contains("headache") || lower.contains("head pain") || lower.contains("migraine") ||
                lower.contains("thala vali") || lower.contains("thalavali") || lower.contains("thalaivalikuthu") || lower.contains("thalaivalikudhu")) {
            entities.setSymptom("headache");
        } else if (lower.contains("hand is hurting") || lower.contains("hand hurts") || lower.contains("hand pain") ||
                lower.contains("arm pain") || lower.contains("wrist pain") || lower.contains("finger pain") ||
                lower.contains("ointment") || lower.contains("pain relief gel") || lower.contains("balm") ||
                lower.contains("kai vali") || lower.contains("kaivalikuthu") || lower.contains("kaivalikudhu") || lower.contains("kai valikudhu")) {
            entities.setSymptom("hand_pain");
        } else if (lower.contains("fever and a sore throat") || lower.contains("fever and sore throat") ||
                (lower.contains("fever") && (lower.contains("throat") || lower.contains("thondai"))) ||
                (lower.contains("kaaichal") && lower.contains("thondai"))) {
            entities.setSymptom("fever_throat");
        } else if (lower.contains("fever") || lower.contains("temperature") || lower.contains("pyrexia") ||
                lower.contains("kaaichal") || lower.contains("kaachal") || lower.contains("kaachil")) {
            entities.setSymptom("fever");
        } else if (lower.contains("cold") || lower.contains("cough") || lower.contains("runny nose") ||
                lower.contains("sneezing") || lower.contains("sali") || lower.contains("irumal")) {
            entities.setSymptom("cold");
        } else if (lower.contains("acidity") || lower.contains("heartburn") || lower.contains("stomach pain") ||
                lower.contains("gastric") || lower.contains("indigestion") || lower.contains("vayiru vali")) {
            entities.setSymptom("acidity");
        }
    }

    private void checkConfirmationOrCancellation(ExtractedQueryEntities entities, String lower) {
        if (lower.matches("(?i)^(yes|confirm|proceed|yes confirm|yes, confirm order|ok confirm|sure confirm|confirm order|aam|seri|yes please).*")) {
            entities.setConfirmation(true);
        } else if (lower.matches("(?i)^(no|cancel|stop|nevermind|vendaam|cancel order).*")) {
            entities.setCancellation(true);
        }

        if (lower.contains("check nearby pharmacies") || lower.contains("check nearby") || lower.contains("check stock")) {
            entities.setCheckPharmacies(true);
        }

        if (lower.contains("difference between") || lower.contains("compare") ||
                lower.contains("which is better") || lower.contains("what is the difference")) {
            entities.setComparison(true);
        }
    }

    private void extractQuantity(ExtractedQueryEntities entities, String lower) {
        Matcher expQtyMatcher = EXPLICIT_QTY_PATTERN.matcher(lower);
        if (expQtyMatcher.find()) {
            try {
                int q = Integer.parseInt(expQtyMatcher.group(1));
                if (q > 0 && q <= 1000) entities.setQuantity(q);
            } catch (NumberFormatException ignored) {}
        } else {
            Matcher unitQtyMatcher = UNIT_QTY_PATTERN.matcher(lower);
            if (unitQtyMatcher.find()) {
                try {
                    int q = Integer.parseInt(unitQtyMatcher.group(1));
                    if (q > 0 && q <= 1000) entities.setQuantity(q);
                } catch (NumberFormatException ignored) {}
            } else {
                entities.setQuantity(1);
            }
        }
    }

    private ChatIntent detectIntent(String lower, ExtractedQueryEntities entities) {
        // Confirmation / Cancellation
        if (entities.isConfirmation()) {
            return ChatIntent.CONFIRM_ORDER;
        }
        if (entities.isCancellation()) {
            return ChatIntent.CANCEL_ORDER;
        }

        // Medicine Comparison
        if (entities.isComparison()) {
            return ChatIntent.MEDICINE_COMPARISON;
        }

        // Order Status tracking
        if (entities.getOrderReference() != null ||
                lower.contains("order status") || lower.contains("track order") ||
                lower.contains("my order") || lower.contains("track request") ||
                lower.contains("enga irukku") || lower.contains("status enna")) {
            return ChatIntent.ORDER_STATUS;
        }

        // Order Medicine workflow
        if (lower.startsWith("order ") || lower.contains("i want to order") || lower.contains("order this medicine") ||
                lower.contains("order medicine") || lower.contains("place order") || lower.contains("order from ") ||
                (lower.contains("order") && entities.getMedicineName() != null) ||
                (lower.contains("buy") && entities.getMedicineName() != null) ||
                (lower.contains("book") && entities.getMedicineName() != null)) {
            return ChatIntent.ORDER_MEDICINE;
        }

        // Symptom Health Advice
        if (entities.getSymptom() != null) {
            return ChatIntent.SYMPTOM_ADVICE;
        }

        // Brand inquiry (e.g. "Do you have Dolo?")
        if (entities.getBrandName() != null && (lower.contains("do you have") || lower.contains("is there") || lower.contains("irukka"))) {
            return ChatIntent.SYMPTOM_ADVICE;
        }

        // Payment Help
        if (lower.contains("payment") || lower.contains("pay") || lower.contains("cash on delivery") ||
                lower.contains("cod") || lower.contains("upi") || lower.contains("kaasu") || lower.contains("panam")) {
            return ChatIntent.PAYMENT_HELP;
        }

        // Delivery Options
        if (lower.contains("can i get this medicine delivered") || lower.contains("delivery available") ||
                lower.contains("door delivery") || lower.contains("home delivery") || lower.contains("delivery irukka") ||
                lower.contains("delivery pannuveengala")) {
            return ChatIntent.DELIVERY_OPTIONS;
        }

        // Pickup Options
        if (lower.contains("pickup") || lower.contains("self pickup") || lower.contains("collect")) {
            return ChatIntent.PICKUP_OPTIONS;
        }

        // Distance & Travel Time
        if (lower.contains("distance") || lower.contains("how far") || lower.contains("travel time") ||
                lower.contains("thooram") || lower.contains("evvalavu thooram")) {
            return ChatIntent.DISTANCE;
        }

        // How to request
        if (lower.contains("how to request") || lower.contains("how to order") ||
                lower.contains("how to book")) {
            return ChatIntent.HOW_TO_REQUEST;
        }

        // Pharmacy Details
        if (lower.contains("pharmacy details") || lower.contains("contact number") ||
                lower.contains("phone number") || lower.contains("hours") || lower.contains("timing") ||
                lower.contains("open now") || lower.contains("chemist details")) {
            return ChatIntent.PHARMACY_DETAILS;
        }

        // Check Stock / Find Medicine
        if (entities.getMedicineName() != null || entities.isCheckPharmacies() || lower.contains("stock") || lower.contains("available") ||
                lower.contains("kidaikuma") || lower.contains("irukka") || lower.contains("marunthu") ||
                lower.contains("marundhu") || lower.contains("medicine") || lower.contains("tablet")) {
            return ChatIntent.CHECK_STOCK;
        }

        // Nearby Pharmacies
        if (lower.contains("nearby") || lower.contains("near me") || lower.contains("near") ||
                lower.contains("pakkathu") || lower.contains("arugil") || lower.contains("kitta") ||
                lower.contains("pharmacy") || lower.contains("medical shop") || lower.contains("chemist")) {
            return ChatIntent.NEARBY_PHARMACIES;
        }

        // Greeting
        if (lower.contains("hi") || lower.contains("hello") || lower.contains("hey") ||
                lower.contains("vanakkam") || lower.contains("namaste") || lower.contains("good morning") ||
                lower.contains("good afternoon") || lower.contains("good evening")) {
            return ChatIntent.GREETING;
        }

        return ChatIntent.UNKNOWN;
    }

    private void extractLocalityAndCoordinates(ExtractedQueryEntities entities, String lower,
                                               Double clientLat, Double clientLng, String clientLocality) {
        // Check for "near my college", "near college", "near university", "near campus"
        if (lower.contains("near my college") || lower.contains("near college") || lower.contains("near campus") || lower.contains("near university")) {
            entities.setLocality("college");
            return;
        }

        // Check known localities in query
        for (Map.Entry<String, double[]> entry : LOCALITY_COORDINATES.entrySet()) {
            if (lower.contains(entry.getKey())) {
                entities.setLocality(capitalize(entry.getKey()));
                entities.setLat(entry.getValue()[0]);
                entities.setLng(entry.getValue()[1]);
                return;
            }
        }

        // Fallback to client location if provided
        if (clientLat != null && clientLng != null) {
            entities.setLat(clientLat);
            entities.setLng(clientLng);
            entities.setLocality(clientLocality != null && !clientLocality.isBlank() ? clientLocality : "Chennai");
            return;
        }

        // Default Central Chennai coordinates
        entities.setLat(13.0827);
        entities.setLng(80.2707);
        if (entities.getLocality() == null) {
            entities.setLocality("Chennai");
        }
    }

    private boolean isStopWord(String w) {
        return List.of("and", "the", "for", "with", "near", "in", "to", "at", "is", "a", "me", "what", "can").contains(w.toLowerCase());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
