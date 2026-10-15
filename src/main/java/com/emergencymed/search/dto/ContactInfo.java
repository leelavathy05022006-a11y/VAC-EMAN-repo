package com.emergencymed.search.dto;

public class ContactInfo {
    private String callUrl;
    private String whatsappUrl;

    public ContactInfo() {
    }

    public ContactInfo(String callUrl, String whatsappUrl) {
        this.callUrl = callUrl;
        this.whatsappUrl = whatsappUrl;
    }

    public String getCallUrl() {
        return callUrl;
    }

    public void setCallUrl(String callUrl) {
        this.callUrl = callUrl;
    }

    public String getWhatsappUrl() {
        return whatsappUrl;
    }

    public void setWhatsappUrl(String whatsappUrl) {
        this.whatsappUrl = whatsappUrl;
    }
}
