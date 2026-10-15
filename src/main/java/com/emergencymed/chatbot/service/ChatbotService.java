package com.emergencymed.chatbot.service;

import com.emergencymed.common.enums.DeliveryStatus;

import com.emergencymed.chatbot.config.ChatbotProperties;
import com.emergencymed.chatbot.dto.*;
import com.emergencymed.chatbot.entity.ChatMessage;
import com.emergencymed.chatbot.entity.ChatSession;
import com.emergencymed.chatbot.llm.ChatbotLlmClient;
import com.emergencymed.chatbot.repository.ChatMessageRepository;
import com.emergencymed.chatbot.repository.ChatSessionRepository;
import com.emergencymed.common.contract.*;
import com.emergencymed.common.dto.*;
import com.emergencymed.common.exception.ApiException;
import com.emergencymed.common.exception.UnauthorizedException;
import com.emergencymed.common.util.GeoUtils;
import com.emergencymed.common.util.MapLinks;
import com.emergencymed.common.util.PhoneUtils;
import com.emergencymed.search.dto.AlternativesResponse;
import com.emergencymed.search.dto.PharmacyMatch;
import com.emergencymed.search.dto.SearchRequest;
import com.emergencymed.search.service.AlternativeSuggestionService;
import com.emergencymed.search.service.OpeningHoursService;
import com.emergencymed.search.service.PharmacySearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private final ChatbotProperties properties;
    private final ChatbotRateLimiter rateLimiter;
    private final ChatbotSanitizer sanitizer;
    private final RuleBasedIntentHandler ruleBasedIntentHandler;
    private final ChatbotLlmClient llmClient;

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    private final CurrentUserProvider currentUserProvider;
    private final MedicineLookup medicineLookup;
    private final PharmacyLookup pharmacyLookup;
    private final InventoryQueryService inventoryQueryService;
    private final PharmacySettingsLookup pharmacySettingsLookup;
    private final DeliveryAvailabilityQuery deliveryAvailabilityQuery;
    private final EmergencyRequestLookup emergencyRequestLookup;

    private final PharmacySearchService pharmacySearchService;
    private final AlternativeSuggestionService alternativeSuggestionService;
    private final OpeningHoursService openingHoursService;

    private final ChatbotHealthKnowledgeService healthKnowledgeService;
    private final ChatbotOrderService chatbotOrderService;

    private final Map<String, ChatConversationContext> sessionContexts = new ConcurrentHashMap<>();

    public ChatbotService(ChatbotProperties properties,
                          ChatbotRateLimiter rateLimiter,
                          ChatbotSanitizer sanitizer,
                          RuleBasedIntentHandler ruleBasedIntentHandler,
                          ChatbotLlmClient llmClient,
                          ChatSessionRepository sessionRepository,
                          ChatMessageRepository messageRepository,
                          CurrentUserProvider currentUserProvider,
                          MedicineLookup medicineLookup,
                          PharmacyLookup pharmacyLookup,
                          InventoryQueryService inventoryQueryService,
                          PharmacySettingsLookup pharmacySettingsLookup,
                          DeliveryAvailabilityQuery deliveryAvailabilityQuery,
                          EmergencyRequestLookup emergencyRequestLookup,
                          PharmacySearchService pharmacySearchService,
                          AlternativeSuggestionService alternativeSuggestionService,
                          OpeningHoursService openingHoursService,
                          ChatbotHealthKnowledgeService healthKnowledgeService,
                          ChatbotOrderService chatbotOrderService) {
        this.properties = properties;
        this.rateLimiter = rateLimiter;
        this.sanitizer = sanitizer;
        this.ruleBasedIntentHandler = ruleBasedIntentHandler;
        this.llmClient = llmClient;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.currentUserProvider = currentUserProvider;
        this.medicineLookup = medicineLookup;
        this.pharmacyLookup = pharmacyLookup;
        this.inventoryQueryService = inventoryQueryService;
        this.pharmacySettingsLookup = pharmacySettingsLookup;
        this.deliveryAvailabilityQuery = deliveryAvailabilityQuery;
        this.emergencyRequestLookup = emergencyRequestLookup;
        this.pharmacySearchService = pharmacySearchService;
        this.alternativeSuggestionService = alternativeSuggestionService;
        this.openingHoursService = openingHoursService;
        this.healthKnowledgeService = healthKnowledgeService;
        this.chatbotOrderService = chatbotOrderService;
    }

    public ChatConversationContext getSessionContext(String sessionId) {
        if (sessionId == null) return null;
        return sessionContexts.get(sessionId);
    }

    private ChatConversationContext getOrCreateContext(String sessionId) {
        return sessionContexts.computeIfAbsent(sessionId, id -> {
            ChatConversationContext ctx = new ChatConversationContext();
            ctx.setSessionId(id);
            return ctx;
        });
    }

    /**
     * Core message pipeline: rate limit -> validate & sanitize -> detect intent -> delegate to domain -> reply -> persist.
     */
    @Transactional
    public ChatbotMessageResponse processMessage(ChatbotMessageRequest request, String clientIp) {
        // 1. Rate Limiting (20 messages per minute per user or IP)
        String rateLimitKey = resolveRateLimitKey(clientIp);
        rateLimiter.checkRateLimit(rateLimitKey);

        // 2. Input Validation (1 to 500 chars)
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "Message cannot be empty.");
        }
        String rawMessage = request.getMessage().trim();
        if (rawMessage.length() > 500) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "Message exceeds 500 characters limit.");
        }

        // Sanitize input: strip HTML and control characters
        String cleanMessage = sanitizer.sanitizeInput(rawMessage);
        if (cleanMessage.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "Message cannot be blank after sanitization.");
        }

        // 3. Session Resolution & Multi-Turn Context Memory
        ChatSession session = resolveOrCreateSession(request.getSessionId());
        ChatConversationContext context = getOrCreateContext(session.getId());

        if (request.getLocality() != null && !request.getLocality().isBlank()) {
            context.setLocality(request.getLocality());
        }
        if (request.getLat() != null && request.getLng() != null) {
            context.setUserLat(request.getLat());
            context.setUserLng(request.getLng());
        }
        if (currentUserProvider.isAuthenticated()) {
            context.setAuthenticatedUserId(currentUserProvider.userId());
        }

        // 4. Intent and Entity Extraction
        ExtractedQueryEntities entities = ruleBasedIntentHandler.analyzeQuery(
                cleanMessage, request.getLat(), request.getLng(), request.getLocality()
        );

        // Multi-Turn Context Memory Fallback:
        // If user says "Yes, check nearby pharmacies" or "Order this medicine" without repeating the medicine name
        if (entities.getMedicineName() == null && context.getLastDiscussedMedicine() != null) {
            if (entities.isCheckPharmacies() || entities.getIntent() == ChatIntent.CHECK_STOCK || entities.getIntent() == ChatIntent.ORDER_MEDICINE) {
                entities.setMedicineName(context.getLastDiscussedMedicine());
            }
        }

        // If the user is currently in an active multi-turn order flow, route subsequent turns to order service
        boolean inOrderFlow = context.isOrderInProgress() || context.isAwaitingOrderConfirmation();
        if (inOrderFlow && !entities.isEmergency()) {
            if (entities.isCancellation()) {
                entities.setIntent(ChatIntent.CANCEL_ORDER);
            } else if (entities.isConfirmation() || (context.isAwaitingOrderConfirmation() && cleanMessage.toLowerCase().matches("(?i).*(yes|confirm|proceed|ok|sure|aam|seri).*"))) {
                entities.setIntent(ChatIntent.CONFIRM_ORDER);
            } else {
                entities.setIntent(ChatIntent.ORDER_MEDICINE);
            }
        }

        // If not an emergency, and LLM is enabled and available, attempt LLM extraction
        if (!entities.isEmergency() && !inOrderFlow && llmClient.isAvailable()) {
            ExtractedQueryEntities llmEntities = llmClient.classifyAndExtract(cleanMessage);
            if (llmEntities != null && llmEntities.getIntent() != null && llmEntities.getIntent() != ChatIntent.UNKNOWN) {
                entities.setIntent(llmEntities.getIntent());
                if (llmEntities.getMedicineName() != null) entities.setMedicineName(llmEntities.getMedicineName());
                if (llmEntities.getQuantity() != null && llmEntities.getQuantity() > 0) entities.setQuantity(llmEntities.getQuantity());
            }
        }

        // 5. Construct Reply based strictly on verified domain data
        ChatbotMessageResponse response = new ChatbotMessageResponse();
        response.setSessionId(session.getId());
        response.setIntent(entities.getIntent());
        response.setQuickReplies(getStandardQuickReplies());

        executeDomainLogic(entities, cleanMessage, context, response);

        // 6. Optional polite rephrasing if LLM is enabled (keeps verified facts intact)
        if (llmClient.isAvailable() && !entities.isEmergency() && !inOrderFlow) {
            response.setReplyText(llmClient.rephraseReply(response.getReplyText()));
        }

        // 7. Store masked conversation into chat_messages (Privacy preserving)
        persistChatHistory(session, entities.getIntent(), cleanMessage, response.getReplyText());

        return response;
    }

    private void executeDomainLogic(ExtractedQueryEntities entities, String cleanMessage,
                                    ChatConversationContext context, ChatbotMessageResponse response) {
        ChatIntent intent = entities.getIntent();

        switch (intent) {
            case EMERGENCY:
                context.resetOrderDraft();
                handleEmergencyIntent(response);
                break;

            case GREETING:
                handleGreetingIntent(response);
                break;

            case SYMPTOM_ADVICE:
                handleSymptomOrBrandGuidance(entities, cleanMessage, context, response);
                break;

            case MEDICINE_COMPARISON:
                healthKnowledgeService.generateMedicineComparison(cleanMessage, response);
                break;

            case ORDER_MEDICINE:
            case CONFIRM_ORDER:
            case CANCEL_ORDER:
                handleOrderConversation(entities, cleanMessage, context, response);
                break;

            case CHECK_STOCK:
            case FIND_MEDICINE:
                handleStockAndMedicineSearch(entities, context, response);
                break;

            case NEARBY_PHARMACIES:
            case DISTANCE:
                handleNearbyPharmacies(entities, response);
                break;

            case PHARMACY_DETAILS:
                handlePharmacyDetails(entities, response);
                break;

            case DELIVERY_OPTIONS:
            case PICKUP_OPTIONS:
            case HOW_TO_REQUEST:
                handleFulfillmentAndHowToRequest(response);
                break;

            case ORDER_STATUS:
                handleOrderStatus(entities, response);
                break;

            case PAYMENT_HELP:
                handlePaymentHelp(response);
                break;

            case UNKNOWN:
            default:
                if (entities.isCheckPharmacies() || (context.getLastDiscussedMedicine() != null && cleanMessage.toLowerCase().contains("check"))) {
                    if (entities.getMedicineName() == null) entities.setMedicineName(context.getLastDiscussedMedicine());
                    handleStockAndMedicineSearch(entities, context, response);
                } else {
                    handleUnknownIntent(response);
                }
                break;
        }
    }

    private void handleSymptomOrBrandGuidance(ExtractedQueryEntities entities, String cleanMessage,
                                              ChatConversationContext context, ChatbotMessageResponse response) {
        String lower = cleanMessage.toLowerCase();
        if (entities.getBrandName() != null && !entities.getBrandName().isBlank() &&
                (entities.getSymptom() == null || lower.contains("what is") || lower.contains("tell me about") ||
                 lower.contains("explain") || lower.contains("do you have"))) {
            healthKnowledgeService.generateBrandExplanation(entities.getBrandName(), response);
            context.setLastDiscussedMedicine(entities.getMedicineName() != null ? entities.getMedicineName() : entities.getBrandName());
        } else {
            healthKnowledgeService.generateSymptomGuidance(entities, cleanMessage, response);
            context.setActiveSymptom(entities.getSymptom());
            // Map common symptoms to standard medicines in context memory
            if ("headache".equalsIgnoreCase(entities.getSymptom()) || "fever".equalsIgnoreCase(entities.getSymptom()) || "fever_throat".equalsIgnoreCase(entities.getSymptom())) {
                context.setLastDiscussedMedicine("Paracetamol 650mg");
            } else if ("hand_pain".equalsIgnoreCase(entities.getSymptom()) || "ointment".equalsIgnoreCase(entities.getSymptom())) {
                context.setLastDiscussedMedicine("Ibuprofen 400mg");
            } else if ("cold".equalsIgnoreCase(entities.getSymptom()) || "cough".equalsIgnoreCase(entities.getSymptom())) {
                context.setLastDiscussedMedicine("Cetirizine 10mg");
            } else if ("acidity".equalsIgnoreCase(entities.getSymptom()) || "stomach_pain".equalsIgnoreCase(entities.getSymptom())) {
                context.setLastDiscussedMedicine("Omeprazole 20mg");
            }
        }
    }

    private void handleOrderConversation(ExtractedQueryEntities entities, String cleanMessage,
                                         ChatConversationContext context, ChatbotMessageResponse response) {
        chatbotOrderService.processOrderConversation(entities, cleanMessage, context, response);
    }

    private void handleEmergencyIntent(ChatbotMessageResponse response) {
        response.setEmergencyAlert(new EmergencyActionDto(
                "CRITICAL_HEALTH_EMERGENCY",
                "Immediate Emergency Care Required",
                "Call 112 or 108 ambulance now or proceed immediately to the nearest emergency department."
        ));
        response.setReplyText(
                "⚠️ CRITICAL EMERGENCY NOTICE:\n" +
                "If you or someone nearby is experiencing a life-threatening medical emergency (e.g. chest pain, severe shortness of breath, unconsciousness, heavy bleeding, or poisoning), PLEASE CALL EMERGENCY SERVICES IMMEDIATELY:\n" +
                "• National Emergency Helpline: 112\n" +
                "• Emergency Ambulance: 108\n\n" +
                "Please do not wait for pharmacy searches or delivery. Seek emergency hospital care immediately."
        );
    }

    private void handleGreetingIntent(ChatbotMessageResponse response) {
        response.setReplyText(
                "Hi! Welcome to Emergency Medicine Availability Network. I am your 24/7 AI Health & Pharmacy Order Assistant.\n\n" +
                "I can help you with:\n" +
                "• Symptom guidance and safe OTC self-care\n" +
                "• Finding medicines and live pharmacy stock across Chennai\n" +
                "• Placing emergency delivery or pickup orders directly in chat\n" +
                "• Pharmacy contact numbers, directions, and order tracking\n\n" +
                "How may I assist you right now?"
        );
        response.setQuickReplies(List.of(
                "I have a headache",
                "My hand is hurting",
                "Do you have Dolo?",
                "Find Paracetamol in Adyar",
                "Track my order"
        ));
    }

    private void handleStockAndMedicineSearch(ExtractedQueryEntities entities,
                                              ChatConversationContext context,
                                              ChatbotMessageResponse response) {
        // Handle "near my college" ambiguous locality
        if ("college".equalsIgnoreCase(entities.getLocality())) {
            response.setReplyText("Which college or university campus are you near in Chennai? (e.g. Anna University Guindy, IIT Madras Adyar, SRM Kattankulathur, or MMC Chennai Central). You can also share your current locality or pincode.");
            response.setQuickReplies(List.of("Anna University (Guindy)", "IIT Madras (Adyar)", "Chennai Central", "Tambaram"));
            return;
        }

        String queryMed = entities.getMedicineName();
        if (queryMed == null || queryMed.isBlank()) {
            if (context.getLastDiscussedMedicine() != null) {
                queryMed = context.getLastDiscussedMedicine();
                entities.setMedicineName(queryMed);
            }
        }

        if (queryMed == null || queryMed.isBlank()) {
            response.setReplyText("Which medication are you looking for? You can tell me the medicine name (for example, 'Paracetamol 650 mg', 'Cetirizine 10mg', or 'Amoxicillin') and your locality.");
            return;
        }

        List<MedicineDto> matches = medicineLookup.searchByName(queryMed, 5);
        if (matches.isEmpty() && queryMed.contains(" ")) {
            // Try matching first word (e.g. "Paracetamol" from "Paracetamol 650mg")
            String firstWord = queryMed.split("\\s+")[0];
            matches = medicineLookup.searchByName(firstWord, 5);
        }

        if (matches.isEmpty()) {
            response.setReplyText(
                    "I could not find \"" + queryMed + "\" in our catalogue. Please check the spelling, or search with the active molecule (generic name)."
            );
            response.setQuickReplies(List.of("Paracetamol 650mg", "Cetirizine 10mg", "ORS Packets", "Omeprazole 20mg"));
            return;
        }

        MedicineDto selectedMed = matches.get(0);
        context.setLastDiscussedMedicine(selectedMed.getName());
        context.setLastDiscussedMedicineId(selectedMed.getId());

        int qty = (entities.getQuantity() != null && entities.getQuantity() > 0) ? entities.getQuantity() : 1;
        double lat = entities.getLat();
        double lng = entities.getLng();

        SearchRequest searchReq = new SearchRequest();
        searchReq.setMedicineId(selectedMed.getId());
        searchReq.setQuantity(qty);
        searchReq.setLat(lat);
        searchReq.setLng(lng);
        searchReq.setRadiusKm(15.0);
        searchReq.setSort("distance");

        List<PharmacyMatch> pharmacyMatches = pharmacySearchService.searchPharmacies(searchReq);

        StringBuilder sb = new StringBuilder();
        if (selectedMed.isRequiresPrescription()) {
            sb.append("⚠️ Rx Prescription Required: Valid prescription will be verified by the pharmacy prior to handover.\n\n");
        }

        if (!pharmacyMatches.isEmpty()) {
            context.setDraftPharmacyId(pharmacyMatches.get(0).getPharmacyId());
            context.setDraftPharmacyName(pharmacyMatches.get(0).getName());

            sb.append("Found ").append(pharmacyMatches.size()).append(" verified pharmacies holding stock for ")
                    .append(selectedMed.getName()).append(" near ").append(entities.getLocality()).append(":\n");

            List<PharmacyCardDto> cards = new ArrayList<>();
            for (PharmacyMatch match : pharmacyMatches) {
                PharmacyCardDto card = buildPharmacyCard(match, selectedMed, qty, lat, lng);
                cards.add(card);
            }
            response.setPharmacyCards(cards);
            sb.append("\nPlease review the pharmacy details below. You can call, navigate, or order directly right here in chat.");

            response.setQuickReplies(List.of(
                    "Order " + selectedMed.getName(),
                    "Check delivery options",
                    "Nearby Chennai Pharmacies",
                    "Track my order"
            ));
        } else {
            // Direct matches 0 -> query wider radius & generic alternatives
            AlternativesResponse alt = alternativeSuggestionService.findAlternatives(selectedMed.getId(), qty, lat, lng, 15.0);
            sb.append("No direct stock found for ").append(selectedMed.getName())
                    .append(" within 15 km in ").append(entities.getLocality())
                    .append(". You may search in a wider radius (up to 50 km) or check nearby localities.\n");

            if (alt.getGenericSubstitutes() != null && !alt.getGenericSubstitutes().isEmpty()) {
                sb.append("\n• Generic Substitutes (Same Active Molecule):\n");
                sb.append("⚠️ Important: Confirm with a doctor or pharmacist before using a substitute.\n");

                List<GenericSubstituteCardDto> subs = alt.getGenericSubstitutes().stream()
                        .map(m -> new GenericSubstituteCardDto(m.getId(), m.getName(), m.getStrength(), m.getDosageForm(), m.getGenericName()))
                        .collect(Collectors.toList());
                response.setSubstitutes(subs);
            }

            if (alt.getWiderRadiusMatches() != null && !alt.getWiderRadiusMatches().isEmpty()) {
                sb.append("\n• Extended Radius Availability:\n");
                List<PharmacyCardDto> widerCards = alt.getWiderRadiusMatches().stream()
                        .map(m -> buildPharmacyCard(m, selectedMed, qty, lat, lng))
                        .collect(Collectors.toList());
                response.setPharmacyCards(widerCards);
            }
        }

        response.setReplyText(sb.toString().trim());
    }

    private void handleNearbyPharmacies(ExtractedQueryEntities entities, ChatbotMessageResponse response) {
        double lat = entities.getLat();
        double lng = entities.getLng();
        List<PharmacyDto> approved = pharmacyLookup.findApprovedWithin(lat, lng, 15.0);

        if (approved.isEmpty()) {
            response.setReplyText("No approved pharmacies found within 15 km of " + entities.getLocality() + ". Try expanding your search radius or selecting a central locality.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Approved pharmacies near ").append(entities.getLocality()).append(" (within 15 km):\n");

        List<PharmacyCardDto> cards = new ArrayList<>();
        for (PharmacyDto p : approved) {
            PharmacyCardDto card = new PharmacyCardDto();
            card.setPharmacyId(p.getId());
            card.setName(p.getName());
            card.setAddress(p.getAddress());
            card.setContactNumber(p.getPhoneNumber());

            double dist = GeoUtils.haversineKm(lat, lng, p.getLatitude(), p.getLongitude());
            card.setDistanceKm(dist);
            int eta = (int) Math.round((dist / 25.0) * 60.0);
            card.setTravelTimeMinutes(eta);
            card.setOpenNow(openingHoursService.isOpenNow(p));

            DeliveryStatus dStatus = deliveryAvailabilityQuery.statusFor(p.getId());
            card.setDeliveryAvailable(dStatus == DeliveryStatus.AVAILABLE || dStatus == DeliveryStatus.BUSY);

            card.setCallUrl(PhoneUtils.formatTelUrl(p.getPhoneNumber()));
            card.setWhatsappUrl(PhoneUtils.formatWhatsappUrl(p.getPhoneNumber(), null, null));
            card.setDirectionsUrl(MapLinks.buildDirectionsUrl(lat, lng, p.getLatitude(), p.getLongitude(), "driving"));
            card.setDetailsUrl("/pharmacy-details.html?id=" + p.getId());

            cards.add(card);
        }
        response.setPharmacyCards(cards);
        sb.append("You can call the pharmacy, get turn-by-turn navigation routes, or view complete pharmacy hours.");
        response.setReplyText(sb.toString());
    }

    private void handlePharmacyDetails(ExtractedQueryEntities entities, ChatbotMessageResponse response) {
        handleNearbyPharmacies(entities, response);
    }

    private void handleFulfillmentAndHowToRequest(ChatbotMessageResponse response) {
        response.setReplyText(
                "📋 How to Request Emergency Medicines:\n" +
                "1. Search for your required medicine by name and locality.\n" +
                "2. Choose a verified pharmacy with available stock.\n" +
                "3. Click \"Request Emergency Delivery\" or \"Reserve for Self-Pickup\" to proceed to the official fulfillment request form (/request/new.html).\n\n" +
                "🔒 Safety Notice: For security and data integrity, all reservations and emergency delivery requests are placed exclusively via the verified request form with OTP protection."
        );
    }

    private void handleOrderStatus(ExtractedQueryEntities entities, ChatbotMessageResponse response) {
        if (!currentUserProvider.isAuthenticated()) {
            response.setReplyText(
                    "🔒 Authentication Required: Please log in to your account to view your emergency order status and tracking details."
            );
            return;
        }

        Long currentUserId = currentUserProvider.userId();
        String refCode = entities.getOrderReference();

        if (refCode != null && !refCode.isBlank()) {
            EmergencyRequestDto order = emergencyRequestLookup.findByReferenceCode(refCode);
            // Strict user isolation: If order does not exist OR belongs to another user, return identical not found
            if (order == null || !currentUserId.equals(order.getRequesterUserId())) {
                response.setReplyText(
                        "No emergency order found matching reference code \"" + refCode + "\". Please check your reference code or review your recent requests."
                );
                return;
            }

            OrderSummaryCardDto orderCard = mapOrderToCard(order);
            response.setOrders(List.of(orderCard));
            response.setReplyText(
                    "Here is the latest status for your request " + order.getReferenceCode() + ":\n" +
                    "• Status: " + order.getStatus() + "\n" +
                    "• Medicine: " + order.getMedicineName() + " (" + order.getQuantity() + " units)\n" +
                    "• Pharmacy: " + order.getPharmacyName() + "\n" +
                    "• Payment: " + order.getPaymentMode() + "\n" +
                    (order.getDeliveryPartnerName() != null ? "• Delivery Partner: " + order.getDeliveryPartnerName() + "\n" : "") +
                    (order.getHandoverOtp() != null ? "• Handover OTP: " + order.getHandoverOtp() + "\n" : "") +
                    "Created on: " + order.getCreatedAt()
            );
        } else {
            List<EmergencyRequestDto> userOrders = emergencyRequestLookup.findByRequesterUserId(currentUserId);
            if (userOrders.isEmpty()) {
                response.setReplyText("You do not have any active emergency medicine requests.");
                return;
            }

            List<OrderSummaryCardDto> cards = userOrders.stream()
                    .map(this::mapOrderToCard)
                    .collect(Collectors.toList());
            response.setOrders(cards);
            response.setReplyText("You have " + userOrders.size() + " emergency medicine requests on record. Select a request below for details:");
        }
    }

    private void handlePaymentHelp(ChatbotMessageResponse response) {
        response.setReplyText(
                "💳 Payment Options on Emergency Medicine Network:\n" +
                "1. Self-Pickup: Pay at the pharmacy counter directly upon collection (Cash or supported in-store card/UPI).\n" +
                "2. Emergency Delivery: Cash on Delivery (COD) to the verified delivery partner upon physical medicine inspection and OTP verification.\n\n" +
                "⚠️ Notice: We do not process online advance payments or store credit card details. All transactions are settled directly upon medicine handover."
        );
    }

    private void handleUnknownIntent(ChatbotMessageResponse response) {
        response.setReplyText(
                "I'm here to help with emergency medicine availability, pharmacy contact numbers, navigation, and order status. " +
                "You can ask me things like:\n" +
                "• \"Is Paracetamol 650 available in Adyar?\"\n" +
                "• \"Find nearby pharmacies in T. Nagar\"\n" +
                "• \"Track my order REQ-2026-001\"\n" +
                "• \"What are the payment options?\""
        );
    }

    private PharmacyCardDto buildPharmacyCard(PharmacyMatch match, MedicineDto medicine, int qty, double userLat, double userLng) {
        PharmacyCardDto card = new PharmacyCardDto();
        card.setPharmacyId(match.getPharmacyId());
        card.setName(match.getName());
        card.setAddress(match.getAddress());

        PharmacyDto pharm = pharmacyLookup.findApproved(match.getPharmacyId()).orElse(null);
        String phone = (pharm != null && pharm.getPhoneNumber() != null) ? pharm.getPhoneNumber() : "";
        card.setContactNumber(phone);
        card.setDistanceKm(match.getDistanceKm());
        card.setTravelTimeMinutes(match.getEtaMinutes());
        card.setMedicineId(medicine.getId());
        card.setMedicineName(medicine.getName());
        card.setMedicineStrength(medicine.getStrength());
        card.setRequestedQuantity(qty);
        card.setAvailableQuantity(match.getAvailableQuantity());
        card.setUnitPrice(match.getUnitPrice());
        card.setStockStatus(match.getStockStatus() != null ? match.getStockStatus() : "IN_STOCK");
        card.setDataSource(match.getDataSource());
        card.setLastUpdatedAt(match.getLastUpdatedAt());
        card.setRequiresPrescription(medicine.isRequiresPrescription());

        // Compute Freshness Label per specification
        card.setDataFreshnessLabel(computeFreshnessLabel(match.getDataSource(), match.getLastUpdatedAt()));

        // Hours & Delivery
        card.setOpenNow(openingHoursService.isOpenNow(pharm));
        boolean deliveryFeasible = match.getDelivery() != null &&
                ("AVAILABLE".equalsIgnoreCase(match.getDelivery().getStatus()) || "BUSY".equalsIgnoreCase(match.getDelivery().getStatus()));
        card.setDeliveryAvailable(deliveryFeasible);
        if (match.getDelivery() != null) {
            card.setDeliveryFee(match.getDelivery().getFee());
        }

        // Links
        if (match.getContact() != null && match.getContact().getCallUrl() != null) {
            card.setCallUrl(match.getContact().getCallUrl());
        } else {
            card.setCallUrl(PhoneUtils.formatTelUrl(phone));
        }

        if (match.getContact() != null && match.getContact().getWhatsappUrl() != null) {
            card.setWhatsappUrl(match.getContact().getWhatsappUrl());
        } else {
            card.setWhatsappUrl(PhoneUtils.formatWhatsappUrl(phone, medicine.getName(), qty));
        }

        if (match.getNavigationUrl() != null && !match.getNavigationUrl().isBlank()) {
            card.setDirectionsUrl(match.getNavigationUrl());
        } else if (pharm != null && pharm.getLatitude() != null && pharm.getLongitude() != null) {
            card.setDirectionsUrl(MapLinks.buildDirectionsUrl(userLat, userLng, pharm.getLatitude(), pharm.getLongitude(), "driving"));
        }
        card.setDetailsUrl("/pharmacy-details.html?id=" + match.getPharmacyId() + "&medicineId=" + medicine.getId() + "&quantity=" + qty);
        card.setRequestUrl("/request/new.html?pharmacyId=" + match.getPharmacyId() + "&medicineId=" + medicine.getId() + "&quantity=" + qty +
                "&fulfilment=" + (deliveryFeasible ? "DELIVERY" : "PICKUP"));

        return card;
    }

    private String computeFreshnessLabel(String dataSource, String lastUpdatedAt) {
        if ("LIVE".equalsIgnoreCase(dataSource)) {
            if (isWithinHours(lastUpdatedAt, properties.getStaleHours())) {
                return "Live, confirmed by the pharmacy";
            }
            return "Availability not verified";
        }
        if ("SAMPLE".equalsIgnoreCase(dataSource)) {
            return "Demo data, not real-time stock";
        }
        return "Availability not verified";
    }

    private boolean isWithinHours(String timestamp, int hours) {
        if (timestamp == null || timestamp.isBlank()) {
            return false;
        }
        try {
            Instant time = Instant.parse(timestamp);
            Duration diff = Duration.between(time, Instant.now());
            return Math.abs(diff.toHours()) <= hours;
        } catch (Exception e) {
            return false;
        }
    }

    private OrderSummaryCardDto mapOrderToCard(EmergencyRequestDto dto) {
        return new OrderSummaryCardDto(
                dto.getReferenceCode(),
                dto.getStatus(),
                dto.getMedicineName(),
                dto.getPharmacyName(),
                dto.getQuantity(),
                dto.getPaymentMode(),
                dto.getDeliveryPartnerName(),
                dto.getCreatedAt()
        );
    }

    private ChatSession resolveOrCreateSession(String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = currentUserProvider.isAuthenticated() ? currentUserProvider.userId() : null;

        if (sessionId != null && !sessionId.isBlank()) {
            Optional<ChatSession> existing = sessionRepository.findById(sessionId.trim());
            if (existing.isPresent()) {
                ChatSession s = existing.get();
                s.setLastActiveAt(now);
                if (userId != null && s.getUserId() == null) {
                    s.setUserId(userId);
                }
                return sessionRepository.save(s);
            }
        }

        String newId = (sessionId != null && !sessionId.isBlank()) ? sessionId.trim() : UUID.randomUUID().toString();
        ChatSession created = new ChatSession(newId, userId, now, now);
        return sessionRepository.save(created);
    }

    private void persistChatHistory(ChatSession session, ChatIntent intent, String userText, String botReply) {
        LocalDateTime now = LocalDateTime.now();
        String maskedUser = sanitizer.maskForStorage(userText);
        String maskedBot = sanitizer.maskForStorage(botReply);

        ChatMessage userMsg = new ChatMessage(session.getId(), "USER", intent.name(), maskedUser, now);
        ChatMessage botMsg = new ChatMessage(session.getId(), "BOT", intent.name(), maskedBot, now.plusNanos(1_000_000));

        messageRepository.save(userMsg);
        messageRepository.save(botMsg);
    }

    private String resolveRateLimitKey(String clientIp) {
        if (currentUserProvider.isAuthenticated() && currentUserProvider.userId() != null) {
            return "user:" + currentUserProvider.userId();
        }
        return "ip:" + (clientIp != null ? clientIp : "127.0.0.1");
    }

    private List<String> getStandardQuickReplies() {
        return List.of(
                "Find a Medicine",
                "Nearby Chennai Pharmacies",
                "Check Medicine Stock",
                "Emergency Medicine Request",
                "Track My Order",
                "Payment Help"
        );
    }

    @Transactional(readOnly = true)
    public List<ChatHistoryItemDto> getUserHistory() {
        if (!currentUserProvider.isAuthenticated()) {
            throw new UnauthorizedException("User must be authenticated to access chat history.");
        }
        Long userId = currentUserProvider.userId();
        Optional<ChatSession> latestSession = sessionRepository.findTopByUserIdOrderByLastActiveAtDesc(userId);
        if (latestSession.isEmpty()) {
            return Collections.emptyList();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return messageRepository.findTop50BySessionIdOrderByCreatedAtAsc(latestSession.get().getId()).stream()
                .map(m -> new ChatHistoryItemDto(
                        m.getId(),
                        m.getSender(),
                        m.getIntent(),
                        m.getContent(),
                        m.getCreatedAt().format(formatter)
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public long clearUserHistory() {
        if (!currentUserProvider.isAuthenticated()) {
            throw new UnauthorizedException("User must be authenticated to clear chat history.");
        }
        Long userId = currentUserProvider.userId();
        List<ChatSession> sessions = sessionRepository.findByUserIdOrderByLastActiveAtDesc(userId);
        if (sessions.isEmpty()) {
            return 0;
        }
        List<String> sessionIds = sessions.stream().map(ChatSession::getId).collect(Collectors.toList());
        for (String sid : sessionIds) {
            sessionContexts.remove(sid);
        }
        messageRepository.deleteBySessionIdIn(sessionIds);
        return sessionRepository.deleteByUserId(userId);
    }
}
