package com.emergencymed.chatbot.dto;

public class ExtractedQueryEntities {
    private ChatIntent intent = ChatIntent.UNKNOWN;
    private String medicineName;
    private String brandName;
    private String symptom;
    private Integer quantity = 1;
    private String locality;
    private Double lat;
    private Double lng;
    private String orderReference;
    private Long pharmacyId;
    private String pharmacyNameCandidate;
    private boolean isEmergency = false;
    private String emergencyDetails;

    // Order flow helpers
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String landmark;
    private String fulfilmentType; // "DELIVERY" or "SELF_PICKUP"
    private boolean isConfirmation = false;
    private boolean isCancellation = false;
    private boolean isCheckPharmacies = false;
    private boolean isComparison = false;

    public ExtractedQueryEntities() {
    }

    public ChatIntent getIntent() {
        return intent;
    }

    public void setIntent(ChatIntent intent) {
        this.intent = intent;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getOrderReference() {
        return orderReference;
    }

    public void setOrderReference(String orderReference) {
        this.orderReference = orderReference;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getPharmacyNameCandidate() {
        return pharmacyNameCandidate;
    }

    public void setPharmacyNameCandidate(String pharmacyNameCandidate) {
        this.pharmacyNameCandidate = pharmacyNameCandidate;
    }

    public boolean isEmergency() {
        return isEmergency;
    }

    public void setEmergency(boolean emergency) {
        isEmergency = emergency;
    }

    public String getEmergencyDetails() {
        return emergencyDetails;
    }

    public void setEmergencyDetails(String emergencyDetails) {
        this.emergencyDetails = emergencyDetails;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getFulfilmentType() {
        return fulfilmentType;
    }

    public void setFulfilmentType(String fulfilmentType) {
        this.fulfilmentType = fulfilmentType;
    }

    public boolean isConfirmation() {
        return isConfirmation;
    }

    public void setConfirmation(boolean confirmation) {
        isConfirmation = confirmation;
    }

    public boolean isCancellation() {
        return isCancellation;
    }

    public void setCancellation(boolean cancellation) {
        isCancellation = cancellation;
    }

    public boolean isCheckPharmacies() {
        return isCheckPharmacies;
    }

    public void setCheckPharmacies(boolean checkPharmacies) {
        isCheckPharmacies = checkPharmacies;
    }

    public boolean isComparison() {
        return isComparison;
    }

    public void setComparison(boolean comparison) {
        isComparison = comparison;
    }
}
