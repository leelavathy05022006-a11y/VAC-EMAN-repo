package com.emergencymed.common.dto;

import java.math.BigDecimal;

public class PharmacyDto {
    private Long id;
    private Long userId;
    private String name;
    private String licenceNumber;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phoneNumber;
    private boolean isApproved;
    private boolean opens24x7;
    private String openingTime;
    private String closingTime;

    public PharmacyDto() {
    }

    public PharmacyDto(Long id, Long userId, String name, String address, BigDecimal lat, BigDecimal lng, String phone, String licenseNumber, boolean isApproved) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.latitude = lat != null ? lat.doubleValue() : null;
        this.longitude = lng != null ? lng.doubleValue() : null;
        this.phoneNumber = phone;
        this.licenceNumber = licenseNumber;
        this.isApproved = isApproved;
    }

    public PharmacyDto(Long id, String name, String licenceNumber, String address, Double latitude, Double longitude, String phoneNumber, boolean isApproved, boolean opens24x7, String openingTime, String closingTime, Long userId) {
        this.id = id;
        this.name = name;
        this.licenceNumber = licenceNumber;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phoneNumber = phoneNumber;
        this.isApproved = isApproved;
        this.opens24x7 = opens24x7;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.userId = userId;
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

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    public String getLicenseNumber() {
        return licenceNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenceNumber = licenseNumber;
    }

    public String getLicenseNo() {
        return licenceNumber;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenceNumber = licenseNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLat() {
        return latitude != null ? BigDecimal.valueOf(latitude) : null;
    }

    public BigDecimal getLng() {
        return longitude != null ? BigDecimal.valueOf(longitude) : null;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhone() {
        return phoneNumber;
    }

    public void setPhone(String phone) {
        this.phoneNumber = phone;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public boolean isOpens24x7() {
        return opens24x7;
    }

    public void setOpens24x7(boolean opens24x7) {
        this.opens24x7 = opens24x7;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
