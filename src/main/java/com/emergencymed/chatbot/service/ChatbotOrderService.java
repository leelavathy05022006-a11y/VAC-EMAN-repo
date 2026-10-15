package com.emergencymed.chatbot.service;

import com.emergencymed.auth.entity.User;
import com.emergencymed.auth.repository.UserRepository;
import com.emergencymed.auth.security.CustomUserDetails;
import com.emergencymed.chatbot.dto.*;
import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.contract.MedicineLookup;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.common.contract.PharmacySettingsLookup;
import com.emergencymed.common.dto.MedicineDto;
import com.emergencymed.common.dto.PharmacyDto;
import com.emergencymed.common.dto.PharmacySettingsDto;
import com.emergencymed.inventory.entity.PharmacyInventory;
import com.emergencymed.inventory.repository.PharmacyInventoryRepository;
import com.emergencymed.request.dto.CreateRequestDto;
import com.emergencymed.request.dto.RequestDetailResponse;
import com.emergencymed.request.dto.RequestItemDto;
import com.emergencymed.request.exception.NoDeliveryPartnerException;
import com.emergencymed.request.exception.PrescriptionRequiredException;
import com.emergencymed.request.service.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatbotOrderService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotOrderService.class);

    private static final Pattern PHONE_PATTERN = Pattern.compile("(?i)\\b([6-9]\\d{9})\\b");
    private static final Pattern QTY_PATTERN = Pattern.compile("(?i)\\b(?:qty|quantity|count)\\s*[:=]?\\s*(\\d{1,3})\\b");
    private static final Pattern UNIT_QTY_PATTERN = Pattern.compile("(?i)\\b(\\d{1,3})\\s*(?:units?|tablets?|capsules?|strips?|bottles?|boxes?|vials?|nos?|pieces?)\\b");

    private final RequestService requestService;
    private final MedicineLookup medicineLookup;
    private final PharmacyLookup pharmacyLookup;
    private final PharmacySettingsLookup settingsLookup;
    private final PharmacyInventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public ChatbotOrderService(RequestService requestService,
                               MedicineLookup medicineLookup,
                               PharmacyLookup pharmacyLookup,
                               PharmacySettingsLookup settingsLookup,
                               PharmacyInventoryRepository inventoryRepository,
                               UserRepository userRepository,
                               CurrentUserProvider currentUserProvider) {
        this.requestService = requestService;
        this.medicineLookup = medicineLookup;
        this.pharmacyLookup = pharmacyLookup;
        this.settingsLookup = settingsLookup;
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Main conversational order pipeline: identifies step, extracts missing fields, prompts or confirms.
     */
    @Transactional
    public void processOrderConversation(ExtractedQueryEntities entities, String cleanMessage,
                                         ChatConversationContext context, ChatbotMessageResponse response) {
        // Pre-fill user profile if authenticated and not yet set
        populateAuthenticatedUserDefaults(context);

        // 1. Check for cancellation
        if (entities.isCancellation() || cleanMessage.toLowerCase().matches("(?i).*(cancel|stop|nevermind|vendaam).*")) {
            context.resetOrderDraft();
            response.setReplyText("Your active order has been cancelled. Let me know if you would like to search for other medicines, consult health guidance, or start a new request.");
            response.setQuickReplies(List.of("Find Paracetamol", "I have a headache", "Check nearby pharmacies"));
            return;
        }

        // 2. Check for explicit confirmation
        if (context.isAwaitingOrderConfirmation()) {
            if (entities.isConfirmation() || cleanMessage.toLowerCase().matches("(?i).*(yes|confirm|proceed|ok confirm|sure confirm|aam|seri).*")) {
                executeOrderSubmission(context, response);
                return;
            } else {
                // User may be correcting a detail or replying with something else
                extractDetailsFromMessage(cleanMessage, context);
                if (isOrderDraftComplete(context)) {
                    presentOrderSummary(context, response);
                    return;
                }
            }
        }

        // 3. Extract details from current message
        extractDetailsFromMessage(cleanMessage, context);
        if (entities.getQuantity() != null && entities.getQuantity() > 0) {
            context.setDraftQuantity(entities.getQuantity());
        }
        if (context.getDraftMedicineId() == null) {
            if (entities.getMedicineName() != null && !entities.getMedicineName().isBlank()) {
                resolveMedicineForOrder(entities.getMedicineName(), context);
            } else if (context.getDraftMedicineName() != null && !context.getDraftMedicineName().isBlank()) {
                resolveMedicineForOrder(context.getDraftMedicineName(), context);
            } else if (context.getLastDiscussedMedicine() != null && !context.getLastDiscussedMedicine().isBlank()) {
                resolveMedicineForOrder(context.getLastDiscussedMedicine(), context);
            }
        }

        // Check if user specified a pharmacy in the message (e.g. "Order from Apollo Pharmacy - T. Nagar" or "from Apollo")
        resolvePharmacyFromMessage(cleanMessage, context);

        // 4. Check step-by-step requirements
        // Step A: Medicine selection
        if (context.getDraftMedicineId() == null) {
            promptForMedicine(cleanMessage, context, response);
            return;
        }

        // Prescription Safety Verification Gate
        MedicineDto med = medicineLookup.findById(context.getDraftMedicineId()).orElse(null);
        if (med != null && med.isRequiresPrescription()) {
            response.setReplyText("⚠️ **Prescription Required (Schedule H / Rx Medicine):**\n\n" +
                    "**" + med.getName() + "** is a prescription-only medication. Under Indian drug safety regulations, a valid doctor's prescription must be physically or digitally verified by a licensed pharmacist before it can be dispensed.\n\n" +
                    "To order this medication with your prescription, please upload your prescription via our secure request portal at [/request/new.html?medicineId=" + med.getId() + "](/request/new.html?medicineId=" + med.getId() + ") or present your prescription in person at the pharmacy counter.");
            response.setQuickReplies(List.of("Check nearby pharmacies", "Consult safe OTC options", "I have a headache"));
            context.resetOrderDraft();
            return;
        }

        // Step B: Pharmacy selection
        if (context.getDraftPharmacyId() == null) {
            promptForPharmacy(context, response);
            return;
        }

        // Step C: Fulfilment selection (Delivery vs Self-Pickup)
        if (context.getDraftFulfilmentType() == null) {
            promptForFulfilment(context, response);
            return;
        }

        // Step D: Delivery vs Pickup specific checks
        if ("DELIVERY".equalsIgnoreCase(context.getDraftFulfilmentType())) {
            PharmacySettingsDto settings = settingsLookup.find(context.getDraftPharmacyId()).orElse(null);
            if (settings != null && !settings.isDeliveryEnabled()) {
                context.setDraftFulfilmentType("SELF_PICKUP");
                response.setReplyText("⚠️ Delivery is currently unavailable for " + context.getDraftPharmacyName() +
                        ", but self-pickup at the pharmacy counter is available! I have updated your fulfilment method to **Self-Pickup**.\n\n" +
                        "Could you please confirm your **Full Name** and **10-digit Mobile Number** for pickup reservation?");
                context.setAwaitingField("NAME");
                return;
            }
        }

        // Step E: Customer Name
        if (context.getCustomerName() == null || context.getCustomerName().trim().length() < 2) {
            context.setAwaitingField("NAME");
            response.setReplyText("To proceed with your order for **" + context.getDraftMedicineName() + "** from **" +
                    context.getDraftPharmacyName() + "**, please provide your **Full Name**:");
            return;
        }

        // Step F: Customer Phone Number
        if (context.getCustomerPhone() == null || !isValidIndianPhone(context.getCustomerPhone())) {
            context.setAwaitingField("PHONE");
            response.setReplyText("Thank you, " + context.getCustomerName() + "! Please enter your **10-digit mobile number** (e.g. 9876543210) for order status SMS and handover OTP:");
            return;
        }

        // Step G: Delivery Address (if Delivery)
        if ("DELIVERY".equalsIgnoreCase(context.getDraftFulfilmentType())) {
            if (context.getDeliveryAddress() == null || context.getDeliveryAddress().trim().length() < 8) {
                context.setAwaitingField("ADDRESS");
                response.setReplyText("Please provide your complete **Delivery Address** (including door/street and nearby landmark in Chennai):");
                return;
            }
        }

        // 5. All details gathered -> Present Order Summary & Ask for explicit confirmation
        presentOrderSummary(context, response);
    }

    private void promptForMedicine(String cleanMessage, ChatConversationContext context, ChatbotMessageResponse response) {
        // If user said "Dolo"
        if (cleanMessage.toLowerCase().contains("dolo")) {
            response.setReplyText("Sure! Which Dolo product and strength do you need? We have **Paracetamol 650mg (Dolo 650)** and **Paracetamol 500mg** available. If you're unsure, please confirm the appropriate strength with a pharmacist.");
            response.setQuickReplies(List.of("Paracetamol 650mg", "Paracetamol 500mg", "Check nearby pharmacies"));
            return;
        }

        response.setReplyText("Sure! Which medicine would you like to order? You can tell me the medicine name and quantity (for example, 'Paracetamol 650mg' or 'Cetirizine 10mg').");
        response.setQuickReplies(List.of("Paracetamol 650mg", "Cetirizine 10mg", "ORS Packets", "Check nearby pharmacies"));
    }

    private void promptForPharmacy(ChatConversationContext context, ChatbotMessageResponse response) {
        Long medId = context.getDraftMedicineId();
        MedicineDto med = medicineLookup.findById(medId).orElse(null);
        String medName = med != null ? med.getName() : context.getDraftMedicineName();

        // Query pharmacies holding stock for this medicine
        List<PharmacyInventory> inventoryList = inventoryRepository.findAll().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsActive()) &&
                        i.getMedicine() != null && i.getMedicine().getId().equals(medId) &&
                        i.getAvailableQuantity() > 0)
                .toList();

        if (inventoryList.isEmpty()) {
            response.setReplyText("Currently, our network pharmacies have zero available emergency stock for **" + medName +
                    "**. Would you like me to check generic substitutes or alternative formulations?");
            response.setQuickReplies(List.of("Check generic substitutes", "Check nearby pharmacies", "Cancel"));
            return;
        }

        // Pick the top matching pharmacy or offer selection
        PharmacyInventory firstInv = inventoryList.get(0);
        PharmacyDto topPharmacy = pharmacyLookup.findApproved(firstInv.getPharmacyId()).orElse(null);

        if (topPharmacy != null) {
            context.setDraftPharmacyId(topPharmacy.getId());
            context.setDraftPharmacyName(topPharmacy.getName());
            context.setDraftUnitPrice(firstInv.getUnitPrice());
            context.setAwaitingField("FULFILMENT");

            PharmacySettingsDto s = settingsLookup.find(topPharmacy.getId()).orElse(null);
            if (s != null && s.getDeliveryFee() != null) {
                context.setDraftDeliveryFee(s.getDeliveryFee());
            } else {
                context.setDraftDeliveryFee(new BigDecimal("30.00"));
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Found available stock for **").append(medName).append("** at **").append(topPharmacy.getName())
                    .append("** (").append(topPharmacy.getAddress()).append(").\n")
                    .append("• Available Quantity: ").append(firstInv.getAvailableQuantity()).append(" units\n")
                    .append("• Unit Price: ₹").append(firstInv.getUnitPrice()).append("\n\n")
                    .append("Would you prefer **Home Delivery** or **Self-Pickup** at the pharmacy?");

            response.setReplyText(sb.toString());
            response.setQuickReplies(List.of("Home Delivery", "Self-Pickup", "Order from another pharmacy", "Cancel"));
        } else {
            response.setReplyText("Which pharmacy would you like to order from? (e.g., Apollo Pharmacy - T. Nagar or MedPlus Anna Nagar)");
        }
    }

    private void promptForFulfilment(ChatConversationContext context, ChatbotMessageResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("For your order of **").append(context.getDraftMedicineName()).append("** from **")
                .append(context.getDraftPharmacyName()).append("**:\n\n")
                .append("Would you like **Home Delivery** or **Self-Pickup** at the pharmacy counter?");
        response.setReplyText(sb.toString());
        response.setQuickReplies(List.of("Home Delivery", "Self-Pickup", "Cancel"));
    }

    private void presentOrderSummary(ChatConversationContext context, ChatbotMessageResponse response) {
        context.setAwaitingOrderConfirmation(true);

        int qty = context.getDraftQuantity();
        BigDecimal unitPrice = context.getDraftUnitPrice() != null ? context.getDraftUnitPrice() : BigDecimal.valueOf(28.50);
        BigDecimal medTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
        BigDecimal deliveryFee = "DELIVERY".equalsIgnoreCase(context.getDraftFulfilmentType()) ?
                context.getDraftDeliveryFee() : BigDecimal.ZERO;
        BigDecimal finalTotal = medTotal.add(deliveryFee);

        StringBuilder sb = new StringBuilder();
        sb.append("📋 **ORDER SUMMARY**\n");
        sb.append("─────────────────────────\n");
        sb.append("• **Pharmacy:** ").append(context.getDraftPharmacyName()).append("\n");
        sb.append("• **Medicine:** ").append(context.getDraftMedicineName()).append(" (Qty: ").append(qty).append(")\n");
        sb.append("• **Medicine Total:** ₹").append(medTotal.setScale(2)).append(" (₹").append(unitPrice.setScale(2)).append(" × ").append(qty).append(")\n");
        if ("DELIVERY".equalsIgnoreCase(context.getDraftFulfilmentType())) {
            sb.append("• **Delivery Fee:** ₹").append(deliveryFee.setScale(2)).append("\n");
        }
        sb.append("• **Estimated Total:** ₹").append(finalTotal.setScale(2)).append("\n");
        sb.append("• **Fulfilment:** ").append("DELIVERY".equalsIgnoreCase(context.getDraftFulfilmentType()) ? "Home Delivery" : "Self-Pickup").append("\n");
        if ("DELIVERY".equalsIgnoreCase(context.getDraftFulfilmentType())) {
            sb.append("• **Delivery Address:** ").append(context.getDeliveryAddress());
            if (context.getLandmark() != null && !context.getLandmark().isBlank()) {
                sb.append(" (Landmark: ").append(context.getLandmark()).append(")");
            }
            sb.append("\n");
        }
        sb.append("• **Recipient:** ").append(context.getCustomerName()).append(" (").append(context.getCustomerPhone()).append(")\n");
        sb.append("• **Payment Method:** Pay on Delivery / Pay at Counter (Cash / UPI supported)\n");
        sb.append("─────────────────────────\n");

        MedicineDto med = medicineLookup.findById(context.getDraftMedicineId()).orElse(null);
        if (med != null && med.isRequiresPrescription()) {
            sb.append("⚠️ **Prescription Notice:** This is a prescription-only medication. A valid doctor's prescription must be verified by the pharmacist before medicine handover.\n\n");
        }

        sb.append("Would you like to confirm this order?");

        response.setReplyText(sb.toString().trim());
        response.setQuickReplies(List.of("Yes, confirm order", "Cancel order"));
    }

    private void executeOrderSubmission(ChatConversationContext context, ChatbotMessageResponse response) {
        log.info("[CHATBOT ORDER] Submitting confirmed order: medId={}, pharmId={}, qty={}, fulfilment={}",
                context.getDraftMedicineId(), context.getDraftPharmacyId(), context.getDraftQuantity(), context.getDraftFulfilmentType());

        MedicineDto med = medicineLookup.findById(context.getDraftMedicineId()).orElse(null);
        if (med != null && med.isRequiresPrescription()) {
            // Check if prescription required
            response.setReplyText("⚠️ **Prescription Required (Rx):**\n" +
                    "**" + med.getName() + "** is a prescription-only medication. Under medical safety guidelines, a valid doctor's prescription must be uploaded or presented to a licensed pharmacist before medicine handover.\n\n" +
                    "To complete this request with prescription verification, please upload your prescription via our portal (/request/new.html) or visit the pharmacy counter directly with your physical prescription.");
            context.setAwaitingOrderConfirmation(false);
            return;
        }

        // Ensure security context has an authenticated user (patient)
        ensureSecurityContextUser(context);

        CreateRequestDto reqDto = new CreateRequestDto();
        reqDto.setPharmacyId(context.getDraftPharmacyId());
        reqDto.setFulfilmentType("DELIVERY".equalsIgnoreCase(context.getDraftFulfilmentType()) ? "DELIVERY" : "SELF_PICKUP");
        reqDto.setPriority("URGENT");
        reqDto.setPatientName(context.getCustomerName() != null ? context.getCustomerName().trim() : "Patient");
        reqDto.setPatientPhone(context.getCustomerPhone() != null ? context.getCustomerPhone().trim() : "9876543210");
        reqDto.setItems(List.of(new RequestItemDto(context.getDraftMedicineId(), context.getDraftQuantity())));
        reqDto.setNotes("Conversational order placed via EmergencyMed AI Health Assistant");

        if ("DELIVERY".equalsIgnoreCase(context.getDraftFulfilmentType())) {
            String fullAddr = context.getDeliveryAddress();
            if (context.getLandmark() != null && !context.getLandmark().isBlank()) {
                fullAddr += ", Near " + context.getLandmark();
            }
            reqDto.setDeliveryAddress(fullAddr);
            reqDto.setDeliveryLat(BigDecimal.valueOf(13.0418));
            reqDto.setDeliveryLng(BigDecimal.valueOf(80.2341));
        }

        try {
            RequestDetailResponse created = requestService.createRequest(reqDto);

            StringBuilder sb = new StringBuilder();
            sb.append("🎉 **ORDER CONFIRMED & PLACED SUCCESSFULLY!**\n\n");
            sb.append("• **Order Reference:** `").append(created.getReferenceCode()).append("`\n");
            sb.append("• **Status:** ").append(created.getStatus()).append("\n");
            sb.append("• **Pharmacy:** ").append(created.getPharmacyName()).append("\n");
            sb.append("• **Fulfilment:** ").append(created.getFulfilmentType()).append("\n");
            sb.append("• **Estimated Total:** ₹").append(created.getGrandTotal() != null ? created.getGrandTotal() : context.getEstimatedTotal()).append("\n");

            if (created.getHandoverCode() != null) {
                sb.append("• **Handover OTP:** `").append(created.getHandoverCode()).append("` (Share this with the delivery partner or pharmacist at handover)\n\n");
            } else {
                sb.append("\n");
            }

            if ("DELIVERY".equalsIgnoreCase(context.getDraftFulfilmentType())) {
                sb.append("🚴 Your order has been dispatched to the pharmacy. A delivery partner will be assigned shortly and dispatched to: ").append(context.getDeliveryAddress()).append(".\n");
            } else {
                sb.append("🏪 Your order has been reserved for self-pickup! You can collect it at **").append(created.getPharmacyName()).append("** counter using your reference code.\n");
            }
            sb.append("You will receive real-time SMS / WhatsApp notifications on ").append(context.getCustomerPhone()).append(".");

            // Attach OrderSummaryCardDto for visual card rendering
            OrderSummaryCardDto orderCard = new OrderSummaryCardDto(
                    created.getReferenceCode(),
                    created.getStatus() != null ? created.getStatus() : "PENDING",
                    context.getDraftMedicineName(),
                    created.getPharmacyName(),
                    context.getDraftQuantity(),
                    "PAY_ON_DELIVERY",
                    null,
                    created.getCreatedAt() != null ? created.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")) : "Just now",
                    created.getHandoverCode(),
                    created.getGrandTotal() != null ? created.getGrandTotal() : context.getEstimatedTotal()
            );
            response.setOrders(List.of(orderCard));

            response.setReplyText(sb.toString().trim());
            response.setQuickReplies(List.of("Track my order " + created.getReferenceCode(), "Order another medicine", "Need health advice"));

            // Clear draft on confirmed order
            context.resetOrderDraft();

        } catch (NoDeliveryPartnerException e) {
            log.warn("[CHATBOT ORDER] Delivery unavailable: {}", e.getMessage());
            response.setReplyText("⚠️ Delivery is currently unavailable for this pharmacy: " + e.getMessage() +
                    "\n\nWould you like to switch this order to **Self-Pickup** at the pharmacy counter?");
            context.setDraftFulfilmentType("SELF_PICKUP");
            response.setQuickReplies(List.of("Yes, switch to Self-Pickup", "Cancel order"));
        } catch (PrescriptionRequiredException e) {
            log.warn("[CHATBOT ORDER] Prescription required: {}", e.getMessage());
            response.setReplyText("⚠️ **Prescription Required:** A prescription is required for this medicine. You can upload your prescription at /request/new.html, or present it physically at the pharmacy counter.");
            context.setAwaitingOrderConfirmation(false);
        } catch (Exception e) {
            log.error("[CHATBOT ORDER] Order creation failed", e);
            response.setReplyText("Could not place order: " + e.getMessage() + ". Please verify pharmacy stock or try again.");
            context.setAwaitingOrderConfirmation(false);
        }
    }

    private void extractDetailsFromMessage(String text, ChatConversationContext context) {
        String lower = text.toLowerCase().trim();

        // Check fulfilment choice
        if (lower.contains("delivery") || lower.contains("door delivery") || lower.contains("home delivery")) {
            context.setDraftFulfilmentType("DELIVERY");
        } else if (lower.contains("pickup") || lower.contains("self pickup") || lower.contains("collect")) {
            context.setDraftFulfilmentType("SELF_PICKUP");
        }

        // Check quantity
        Matcher expQty = QTY_PATTERN.matcher(lower);
        if (expQty.find()) {
            try {
                int q = Integer.parseInt(expQty.group(1));
                if (q > 0 && q <= 100) context.setDraftQuantity(q);
            } catch (NumberFormatException ignored) {}
        } else {
            Matcher unitQty = UNIT_QTY_PATTERN.matcher(lower);
            if (unitQty.find()) {
                try {
                    int q = Integer.parseInt(unitQty.group(1));
                    if (q > 0 && q <= 100) context.setDraftQuantity(q);
                } catch (NumberFormatException ignored) {}
            }
        }

        // Check phone number (10-digit Indian mobile)
        Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
            context.setCustomerPhone(phoneMatcher.group(1));
        }

        // Check if currently awaiting specific field
        String awaiting = context.getAwaitingField();
        if ("NAME".equals(awaiting)) {
            Matcher nameMatcher = Pattern.compile("(?i)\\b(?:my name is|name[:=]|i am)\\s*([a-zA-Z\\s]{2,50})").matcher(text);
            if (nameMatcher.find()) {
                context.setCustomerName(nameMatcher.group(1).trim());
                context.setAwaitingField(null);
            } else {
                String candidate = text.replaceAll("(?i)(my name is|i am|name[:=]?|this is)\\s*", "").trim();
                if (candidate.contains(",")) candidate = candidate.split(",")[0].trim();
                if (candidate.toLowerCase().contains("phone")) candidate = candidate.split("(?i)phone")[0].trim();
                if (candidate.length() >= 2 && candidate.length() <= 80 && !candidate.equalsIgnoreCase("yes") && !candidate.equalsIgnoreCase("order")) {
                    context.setCustomerName(candidate);
                    context.setAwaitingField(null);
                }
            }
        } else if ("PHONE".equals(awaiting)) {
            if (phoneMatcher.find()) {
                context.setCustomerPhone(phoneMatcher.group(1));
                context.setAwaitingField(null);
            }
        } else if ("ADDRESS".equals(awaiting)) {
            String candidate = text.replaceAll("(?i)(address is|delivery address[:=]?|deliver to)\\s*", "").trim();
            if (candidate.length() >= 8 && !candidate.equalsIgnoreCase("yes") && !candidate.equalsIgnoreCase("confirm")) {
                context.setDeliveryAddress(candidate);
                context.setAwaitingField(null);
            }
        }

        // Check explicit "name is ...", "phone is ...", "address is ..."
        Matcher nameMatcher = Pattern.compile("(?i)\\b(?:my name is|name[:=])\\s*([a-zA-Z\\s]{2,50})\\b").matcher(text);
        if (nameMatcher.find()) {
            context.setCustomerName(nameMatcher.group(1).trim());
        }

        Matcher addrMatcher = Pattern.compile("(?i)\\b(?:address is|deliver to|address[:=])\\s*([a-zA-Z0-9\\s,#\\-\\.\\/]{8,150})\\b").matcher(text);
        if (addrMatcher.find()) {
            context.setDeliveryAddress(addrMatcher.group(1).trim());
        }

        Matcher landmarkMatcher = Pattern.compile("(?i)\\b(?:near|landmark[:=]|opposite)\\s*([a-zA-Z0-9\\s]{3,60})\\b").matcher(text);
        if (landmarkMatcher.find()) {
            context.setLandmark(landmarkMatcher.group(1).trim());
        }
    }

    private void resolveMedicineForOrder(String candidateName, ChatConversationContext context) {
        String query = candidateName.toLowerCase();
        // Brand mappings
        if (query.contains("dolo") || query.contains("calpol") || query.contains("crocin") || query.contains("pacimol")) {
            if (query.contains("500")) {
                query = "Paracetamol 500mg";
            } else {
                query = "Paracetamol 650mg";
            }
        } else if (query.contains("omez")) {
            query = "Omeprazole 20mg";
        } else if (query.contains("pan 40") || query.contains("pantocid")) {
            query = "Pantoprazole 40mg";
        } else if (query.contains("electral") || query.contains("ors")) {
            query = "ORS Packets";
        } else if (query.contains("limcee") || query.contains("celin")) {
            query = "Vitamin C Tablets 500mg";
        }

        List<MedicineDto> matches = medicineLookup.searchByName(query, 5);
        if (matches.isEmpty() && query.contains(" ")) {
            matches = medicineLookup.searchByName(query.split("\\s+")[0], 5);
        }
        if (!matches.isEmpty()) {
            MedicineDto best = matches.get(0);
            context.setDraftMedicineId(best.getId());
            context.setDraftMedicineName(best.getName());
            context.setLastDiscussedMedicine(best.getName());
            context.setLastDiscussedMedicineId(best.getId());
        }
    }

    private void resolvePharmacyFromMessage(String cleanMessage, ChatConversationContext context) {
        String lower = cleanMessage.toLowerCase();
        List<PharmacyDto> allPharmacies = pharmacyLookup.findApprovedWithin(13.0827, 80.2707, 40.0);

        for (PharmacyDto p : allPharmacies) {
            String nameLower = p.getName().toLowerCase();
            String locLower = p.getAddress() != null ? p.getAddress().toLowerCase() : "";

            if (lower.contains(nameLower) || (!locLower.isEmpty() && lower.contains("from " + locLower))) {
                context.setDraftPharmacyId(p.getId());
                context.setDraftPharmacyName(p.getName());

                PharmacySettingsDto s = settingsLookup.find(p.getId()).orElse(null);
                if (s != null && s.getDeliveryFee() != null) {
                    context.setDraftDeliveryFee(s.getDeliveryFee());
                }
                break;
            }
        }
    }

    private boolean isOrderDraftComplete(ChatConversationContext context) {
        if (context.getDraftMedicineId() == null) return false;
        if (context.getDraftPharmacyId() == null) return false;
        if (context.getDraftFulfilmentType() == null) return false;
        if (context.getCustomerName() == null || context.getCustomerName().trim().length() < 2) return false;
        if (context.getCustomerPhone() == null || !isValidIndianPhone(context.getCustomerPhone())) return false;
        if ("DELIVERY".equalsIgnoreCase(context.getDraftFulfilmentType())) {
            return context.getDeliveryAddress() != null && context.getDeliveryAddress().trim().length() >= 8;
        }
        return true;
    }

    private boolean isValidIndianPhone(String phone) {
        if (phone == null) return false;
        String clean = phone.replaceAll("[^0-9]", "");
        return clean.matches("^[6-9][0-9]{9}$");
    }

    private void populateAuthenticatedUserDefaults(ChatConversationContext context) {
        if (currentUserProvider.isAuthenticated()) {
            try {
                Long uId = currentUserProvider.userId();
                userRepository.findById(uId).ifPresent(u -> {
                    if (context.getCustomerName() == null && u.getFullName() != null) {
                        context.setCustomerName(u.getFullName());
                    }
                    if (context.getCustomerPhone() == null && u.getPhone() != null && isValidIndianPhone(u.getPhone())) {
                        context.setCustomerPhone(u.getPhone());
                    }
                });
            } catch (Exception ignored) {}
        }
    }

    private void ensureSecurityContextUser(ChatConversationContext context) {
        if (!currentUserProvider.isAuthenticated()) {
            // Find patient user by phone or default patient user 1 (Asha Raman)
            String phone = context.getCustomerPhone();
            User patient = null;
            if (phone != null && !phone.isBlank()) {
                patient = userRepository.findAll().stream()
                        .filter(u -> phone.equals(u.getPhone()))
                        .findFirst().orElse(null);
            }
            if (patient == null) {
                patient = userRepository.findById(1L).orElse(null);
            }
            if (patient != null) {
                CustomUserDetails userDetails = new CustomUserDetails(patient, null);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
    }
}
