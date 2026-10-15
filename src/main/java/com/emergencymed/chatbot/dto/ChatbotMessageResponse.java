package com.emergencymed.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatbotMessageResponse {
    private String sessionId;
    private ChatIntent intent;
    private String replyText;
    private List<PharmacyCardDto> pharmacyCards = new ArrayList<>();
    private List<GenericSubstituteCardDto> substitutes = new ArrayList<>();
    private List<OrderSummaryCardDto> orders = new ArrayList<>();
    private EmergencyActionDto emergencyAlert;
    private List<String> quickReplies = new ArrayList<>();
    private String notice;

    public ChatbotMessageResponse() {
    }

    public ChatbotMessageResponse(String sessionId, ChatIntent intent, String replyText) {
        this.sessionId = sessionId;
        this.intent = intent;
        this.replyText = replyText;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ChatIntent getIntent() {
        return intent;
    }

    public void setIntent(ChatIntent intent) {
        this.intent = intent;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public List<PharmacyCardDto> getPharmacyCards() {
        return pharmacyCards;
    }

    public void setPharmacyCards(List<PharmacyCardDto> pharmacyCards) {
        this.pharmacyCards = pharmacyCards;
    }

    public List<GenericSubstituteCardDto> getSubstitutes() {
        return substitutes;
    }

    public void setSubstitutes(List<GenericSubstituteCardDto> substitutes) {
        this.substitutes = substitutes;
    }

    public List<OrderSummaryCardDto> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderSummaryCardDto> orders) {
        this.orders = orders;
    }

    public EmergencyActionDto getEmergencyAlert() {
        return emergencyAlert;
    }

    public void setEmergencyAlert(EmergencyActionDto emergencyAlert) {
        this.emergencyAlert = emergencyAlert;
    }

    public List<String> getQuickReplies() {
        return quickReplies;
    }

    public void setQuickReplies(List<String> quickReplies) {
        this.quickReplies = quickReplies;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }
}
