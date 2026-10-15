package com.emergencymed.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatbotMessageRequest {

    @NotBlank(message = "Message must not be blank")
    @Size(min = 1, max = 500, message = "Message length must be between 1 and 500 characters")
    private String message;

    private String sessionId;
    private Double lat;
    private Double lng;
    private String city;
    private String locality;

    public ChatbotMessageRequest() {
    }

    public ChatbotMessageRequest(String message, String sessionId, Double lat, Double lng, String city, String locality) {
        this.message = message;
        this.sessionId = sessionId;
        this.lat = lat;
        this.lng = lng;
        this.city = city;
        this.locality = locality;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }
}
