package com.emergencymed.request.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class CreateRequestDto {

    @NotNull(message = "Pharmacy ID is required")
    private Long pharmacyId;

    @NotBlank(message = "Fulfilment type is required")
    @Pattern(regexp = "^(DELIVERY|SELF_PICKUP)$", message = "Fulfilment type must be DELIVERY or SELF_PICKUP")
    private String fulfilmentType;

    @NotBlank(message = "Priority is required")
    @Pattern(regexp = "^(CRITICAL|URGENT)$", message = "Priority must be CRITICAL or URGENT")
    private String priority;

    @NotEmpty(message = "At least one medicine item is required")
    @Size(min = 1, max = 5, message = "Must contain between 1 and 5 medicines")
    @Valid
    private List<RequestItemDto> items;

    @NotBlank(message = "Patient name is required")
    @Size(max = 100, message = "Patient name must not exceed 100 characters")
    private String patientName;

    @NotBlank(message = "Patient phone is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    private String patientPhone;

    @Size(max = 100, message = "Doctor name must not exceed 100 characters")
    private String doctorName;

    @Size(max = 40, message = "Doctor registration number must not exceed 40 characters")
    private String doctorRegNo;

    private String deliveryAddress;

    private BigDecimal deliveryLat;

    private BigDecimal deliveryLng;

    private Long prescriptionId;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    public CreateRequestDto() {
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getFulfilmentType() {
        return fulfilmentType;
    }

    public void setFulfilmentType(String fulfilmentType) {
        this.fulfilmentType = fulfilmentType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<RequestItemDto> getItems() {
        return items;
    }

    public void setItems(List<RequestItemDto> items) {
        this.items = items;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorRegNo() {
        return doctorRegNo;
    }

    public void setDoctorRegNo(String doctorRegNo) {
        this.doctorRegNo = doctorRegNo;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public BigDecimal getDeliveryLat() {
        return deliveryLat;
    }

    public void setDeliveryLat(BigDecimal deliveryLat) {
        this.deliveryLat = deliveryLat;
    }

    public BigDecimal getDeliveryLng() {
        return deliveryLng;
    }

    public void setDeliveryLng(BigDecimal deliveryLng) {
        this.deliveryLng = deliveryLng;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
