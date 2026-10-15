package com.emergencymed.search.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_logs", indexes = {
    @Index(name = "idx_contact_logs_pharmacy_created", columnList = "pharmacy_id, created_at")
})
public class ContactLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "pharmacy_id", nullable = false)
    private Long pharmacyId;

    @Column(name = "medicine_id")
    private Long medicineId;

    @Column(name = "contact_type", length = 10, nullable = false)
    private String contactType; // 'CALL', 'WHATSAPP', 'NAVIGATION'

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ContactLog() {
    }

    public ContactLog(Long userId, Long pharmacyId, Long medicineId, String contactType, LocalDateTime createdAt) {
        this.userId = userId;
        this.pharmacyId = pharmacyId;
        this.medicineId = medicineId;
        this.contactType = contactType;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
