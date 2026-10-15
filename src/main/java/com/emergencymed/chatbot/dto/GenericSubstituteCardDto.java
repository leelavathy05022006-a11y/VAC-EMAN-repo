package com.emergencymed.chatbot.dto;

public class GenericSubstituteCardDto {
    private Long medicineId;
    private String name;
    private String strength;
    private String dosageForm;
    private String genericName;

    public GenericSubstituteCardDto() {
    }

    public GenericSubstituteCardDto(Long medicineId, String name, String strength, String dosageForm, String genericName) {
        this.medicineId = medicineId;
        this.name = name;
        this.strength = strength;
        this.dosageForm = dosageForm;
        this.genericName = genericName;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }
}
