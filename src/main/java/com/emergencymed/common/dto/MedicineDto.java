package com.emergencymed.common.dto;

import java.math.BigDecimal;

public class MedicineDto {
    private Long id;
    private String name;
    private String genericName;
    private String manufacturer;
    private String dosageForm;
    private String strength;
    private String description;
    private boolean requiresPrescription;
    private boolean isActive = true;
    private BigDecimal unitPrice;

    public MedicineDto() {
    }

    public MedicineDto(Long id, String name, String genericName, String dosageForm, String strength, String manufacturer, boolean requiresPrescription, BigDecimal unitPrice) {
        this.id = id;
        this.name = name;
        this.genericName = genericName;
        this.dosageForm = dosageForm;
        this.strength = strength;
        this.manufacturer = manufacturer;
        this.requiresPrescription = requiresPrescription;
        this.unitPrice = unitPrice;
        this.isActive = true;
    }

    public MedicineDto(Long id, String name, String genericName, String manufacturer, String dosageForm, String strength, String description, boolean requiresPrescription, boolean isActive) {
        this.id = id;
        this.name = name;
        this.genericName = genericName;
        this.manufacturer = manufacturer;
        this.dosageForm = dosageForm;
        this.strength = strength;
        this.description = description;
        this.requiresPrescription = requiresPrescription;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequiresPrescription() {
        return requiresPrescription;
    }

    public void setRequiresPrescription(boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
