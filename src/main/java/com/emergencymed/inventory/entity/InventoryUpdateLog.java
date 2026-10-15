package com.emergencymed.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_update_logs")
public class InventoryUpdateLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "pharmacy_id", nullable = false)
    private Long pharmacyId;

    @Column(name = "update_source", nullable = false, length = 30)
    private String updateSource; // MANUAL_DASHBOARD, API_INTEGRATION, SCHEDULED_SYNC

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "log_message", length = 255)
    private String logMessage;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    public InventoryUpdateLog() {}

    public InventoryUpdateLog(Long inventoryId, Long pharmacyId, String updateSource, String status, String logMessage) {
        this.inventoryId = inventoryId;
        this.pharmacyId = pharmacyId;
        this.updateSource = updateSource;
        this.status = status;
        this.logMessage = logMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getUpdateSource() {
        return updateSource;
    }

    public void setUpdateSource(String updateSource) {
        this.updateSource = updateSource;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
