package com.emergencymed.common.dto;

import java.math.BigDecimal;

public class PharmacySettingsDto {
    private Long pharmacyId;
    private boolean deliveryEnabled = false;
    private BigDecimal deliveryRadiusKm = BigDecimal.valueOf(10.0);
    private BigDecimal deliveryFee = BigDecimal.valueOf(30.00);
    private boolean opens24x7 = false;
    private String openingTime = "08:00";
    private String closingTime = "22:00";
    private String contactPhone;
    private String whatsappNumber;
    private boolean acceptsEmergencyOrders = true;

    public PharmacySettingsDto() {
    }

    public PharmacySettingsDto(Long pharmacyId, boolean deliveryEnabled, BigDecimal deliveryRadiusKm, BigDecimal deliveryFee) {
        this.pharmacyId = pharmacyId;
        this.deliveryEnabled = deliveryEnabled;
        this.deliveryRadiusKm = deliveryRadiusKm;
        this.deliveryFee = deliveryFee;
    }

    public PharmacySettingsDto(Long pharmacyId, String contactPhone, String whatsappNumber, boolean deliveryEnabled, BigDecimal deliveryRadiusKm, BigDecimal deliveryFee, boolean opens24x7, String openingTime, String closingTime) {
        this.pharmacyId = pharmacyId;
        this.contactPhone = contactPhone;
        this.whatsappNumber = whatsappNumber;
        this.deliveryEnabled = deliveryEnabled;
        this.deliveryRadiusKm = deliveryRadiusKm;
        this.deliveryFee = deliveryFee;
        this.opens24x7 = opens24x7;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public boolean isDeliveryEnabled() {
        return deliveryEnabled;
    }

    public void setDeliveryEnabled(boolean deliveryEnabled) {
        this.deliveryEnabled = deliveryEnabled;
    }

    public BigDecimal getDeliveryRadiusKm() {
        return deliveryRadiusKm;
    }

    public void setDeliveryRadiusKm(BigDecimal deliveryRadiusKm) {
        this.deliveryRadiusKm = deliveryRadiusKm;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public boolean isOpens24x7() {
        return opens24x7;
    }

    public void setOpens24x7(boolean opens24x7) {
        this.opens24x7 = opens24x7;
    }

    public boolean isOpen24Hours() {
        return opens24x7;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public void setWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
    }

    public boolean isAcceptsEmergencyOrders() {
        return acceptsEmergencyOrders;
    }

    public void setAcceptsEmergencyOrders(boolean acceptsEmergencyOrders) {
        this.acceptsEmergencyOrders = acceptsEmergencyOrders;
    }
}
