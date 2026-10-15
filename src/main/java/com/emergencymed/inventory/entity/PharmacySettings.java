package com.emergencymed.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "pharmacy_settings")
public class PharmacySettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pharmacy_id", nullable = false, unique = true)
    private Long pharmacyId;

    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

    @Column(name = "whatsapp_number", length = 15)
    private String whatsappNumber;

    @Column(name = "delivery_enabled", nullable = false)
    private Boolean deliveryEnabled = false;

    @Column(name = "delivery_radius_km", precision = 4, scale = 1)
    private BigDecimal deliveryRadiusKm;

    @Column(name = "delivery_fee", precision = 8, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "opens_24x7", nullable = false)
    private Boolean opens24x7 = false;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "accepts_emergency_orders", nullable = false)
    private Boolean acceptsEmergencyOrders = true;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public PharmacySettings() {}

    public PharmacySettings(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
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

    public Boolean getDeliveryEnabled() {
        return deliveryEnabled;
    }

    public void setDeliveryEnabled(Boolean deliveryEnabled) {
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

    public Boolean getOpens24x7() {
        return opens24x7;
    }

    public void setOpens24x7(Boolean opens24x7) {
        this.opens24x7 = opens24x7;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public Boolean getAcceptsEmergencyOrders() {
        return acceptsEmergencyOrders;
    }

    public void setAcceptsEmergencyOrders(Boolean acceptsEmergencyOrders) {
        this.acceptsEmergencyOrders = acceptsEmergencyOrders;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
