package com.emergencymed.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "medicines",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_medicine_name_strength_form", columnNames = {"name", "strength", "dosage_form"})
    }
)
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private MedicineCategory category;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "generic_name", nullable = false, length = 120)
    private String genericName;

    @Column(nullable = false, length = 40)
    private String strength;

    @Column(name = "dosage_form", nullable = false, length = 20)
    private String dosageForm;

    @Column(nullable = false, length = 120)
    private String manufacturer;

    @Column(name = "requires_prescription", nullable = false)
    private Boolean requiresPrescription = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Medicine() {}

    public Medicine(MedicineCategory category, String name, String genericName, String strength, String dosageForm, String manufacturer, Boolean requiresPrescription, Boolean isActive) {
        this.category = category;
        this.name = name;
        this.genericName = genericName;
        this.strength = strength;
        this.dosageForm = dosageForm;
        this.manufacturer = manufacturer;
        this.requiresPrescription = requiresPrescription != null ? requiresPrescription : false;
        this.isActive = isActive != null ? isActive : true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MedicineCategory getCategory() {
        return category;
    }

    public void setCategory(MedicineCategory category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Boolean getRequiresPrescription() {
        return requiresPrescription;
    }

    public void setRequiresPrescription(Boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
