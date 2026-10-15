package com.emergencymed.request.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_availability")
public class DeliveryAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partner_user_id", nullable = false, unique = true)
    private Long partnerUserId;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = false;

    @Column(name = "current_lat", precision = 9, scale = 6)
    private BigDecimal currentLat;

    @Column(name = "current_lng", precision = 9, scale = 6)
    private BigDecimal currentLng;

    @Column(name = "last_location_at")
    private LocalDateTime lastLocationAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public DeliveryAvailability() {
    }

    public DeliveryAvailability(Long partnerUserId, boolean isAvailable, BigDecimal currentLat, BigDecimal currentLng, LocalDateTime lastLocationAt, LocalDateTime updatedAt) {
        this.partnerUserId = partnerUserId;
        this.isAvailable = isAvailable;
        this.currentLat = currentLat;
        this.currentLng = currentLng;
        this.lastLocationAt = lastLocationAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPartnerUserId() {
        return partnerUserId;
    }

    public void setPartnerUserId(Long partnerUserId) {
        this.partnerUserId = partnerUserId;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public BigDecimal getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(BigDecimal currentLat) {
        this.currentLat = currentLat;
    }

    public BigDecimal getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(BigDecimal currentLng) {
        this.currentLng = currentLng;
    }

    public LocalDateTime getLastLocationAt() {
        return lastLocationAt;
    }

    public void setLastLocationAt(LocalDateTime lastLocationAt) {
        this.lastLocationAt = lastLocationAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
