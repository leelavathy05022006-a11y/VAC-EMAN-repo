package com.emergencymed.chatbot.dto;

public class EmergencyActionDto {
    private String emergencyType;
    private String title;
    private String description;
    private String ambulanceNumber;
    private String nationalEmergencyNumber;

    public EmergencyActionDto() {
        this.ambulanceNumber = "108";
        this.nationalEmergencyNumber = "112";
    }

    public EmergencyActionDto(String emergencyType, String title, String description) {
        this.emergencyType = emergencyType;
        this.title = title;
        this.description = description;
        this.ambulanceNumber = "108";
        this.nationalEmergencyNumber = "112";
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmbulanceNumber() {
        return ambulanceNumber;
    }

    public void setAmbulanceNumber(String ambulanceNumber) {
        this.ambulanceNumber = ambulanceNumber;
    }

    public String getNationalEmergencyNumber() {
        return nationalEmergencyNumber;
    }

    public void setNationalEmergencyNumber(String nationalEmergencyNumber) {
        this.nationalEmergencyNumber = nationalEmergencyNumber;
    }
}
