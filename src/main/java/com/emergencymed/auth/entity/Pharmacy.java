package com.emergencymed.auth.entity;

import com.emergencymed.common.enums.ApprovalStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pharmacies")
public class Pharmacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "license_no", length = 50, unique = true)
    private String licenseNo;

    @Column(name = "license_number", length = 80)
    private String licenseNumber;

    @Column(name = "gst_no", length = 20)
    private String gstNo;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 80)
    private String locality;

    @Column(nullable = false, length = 60)
    private String city = "Chennai";

    @Column(length = 10)
    private String pincode;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "opening_hours", length = 100)
    private String openingHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 15)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "is_approved")
    private Boolean isApproved = false;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedByUser;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_demo", nullable = false)
    private boolean isDemo = false;

    @Column(name = "delivery_available", nullable = false)
    private boolean deliveryAvailable = false;

    @Column(name = "data_source", nullable = false, length = 25)
    private String dataSource = "LIVE";

    @Column(name = "facility_type", length = 30)
    private String facilityType = "PHARMACY";

    @Column(name = "provider_place_id", length = 100)
    private String providerPlaceId;

    @Column(length = 255)
    private String website;

    @Column(length = 120)
    private String email;

    @Column(name = "verification_status", length = 20)
    private String verificationStatus = "VERIFIED";

    @Column(name = "verification_notes", length = 255)
    private String verificationNotes;

    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Pharmacy() {}

    public Pharmacy(User user, String name, String licenseNo, String address, Double latitude, Double longitude) {
        this.user = user;
        this.name = name;
        this.licenseNo = licenseNo;
        this.licenseNumber = licenseNo;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLicenseNo() { return licenseNo != null ? licenseNo : licenseNumber; }
    public void setLicenseNo(String licenseNo) { 
        this.licenseNo = licenseNo; 
        if (this.licenseNumber == null) this.licenseNumber = licenseNo;
    }

    public String getLicenseNumber() { return licenseNumber != null ? licenseNumber : licenseNo; }
    public void setLicenseNumber(String licenseNumber) { 
        this.licenseNumber = licenseNumber; 
        if (this.licenseNo == null) this.licenseNo = licenseNumber;
    }

    public String getGstNo() { return gstNo; }
    public void setGstNo(String gstNo) { this.gstNo = gstNo; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPincode() { return pincode != null ? pincode : postalCode; }
    public void setPincode(String pincode) { 
        this.pincode = pincode; 
        if (this.postalCode == null) this.postalCode = pincode;
    }

    public String getPostalCode() { return postalCode != null ? postalCode : pincode; }
    public void setPostalCode(String postalCode) { 
        this.postalCode = postalCode; 
        if (this.pincode == null) this.pincode = postalCode;
    }

    public String getContactPhone() { return contactPhone != null ? contactPhone : phoneNumber; }
    public void setContactPhone(String contactPhone) { 
        this.contactPhone = contactPhone; 
        if (this.phoneNumber == null) this.phoneNumber = contactPhone;
    }

    public String getPhoneNumber() { return phoneNumber != null ? phoneNumber : contactPhone; }
    public void setPhoneNumber(String phoneNumber) { 
        this.phoneNumber = phoneNumber; 
        if (this.contactPhone == null) this.contactPhone = phoneNumber;
    }

    public String getPhone() { return getContactPhone(); }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude != null ? latitude.doubleValue() : null; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude != null ? longitude.doubleValue() : null; }

    public BigDecimal getLat() { return latitude != null ? BigDecimal.valueOf(latitude) : null; }
    public BigDecimal getLng() { return longitude != null ? BigDecimal.valueOf(longitude) : null; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { 
        this.approvalStatus = approvalStatus; 
        this.isApproved = (approvalStatus == ApprovalStatus.APPROVED);
    }

    public Boolean getIsApproved() { return isApproved != null ? isApproved : (approvalStatus == ApprovalStatus.APPROVED); }
    public void setIsApproved(Boolean isApproved) { 
        this.isApproved = isApproved; 
        if (Boolean.TRUE.equals(isApproved) && this.approvalStatus != ApprovalStatus.APPROVED) {
            this.approvalStatus = ApprovalStatus.APPROVED;
        }
    }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public User getApprovedByUser() { return approvedByUser; }
    public void setApprovedByUser(User approvedByUser) { this.approvedByUser = approvedByUser; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public boolean isActive() { return isActive; }
    public Boolean getIsActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public void setIsActive(Boolean active) { isActive = active != null ? active : true; }

    public boolean isDemo() { return isDemo; }
    public void setDemo(boolean demo) { isDemo = demo; }

    public boolean isDeliveryAvailable() { return deliveryAvailable; }
    public Boolean getDeliveryAvailable() { return deliveryAvailable; }
    public void setDeliveryAvailable(boolean deliveryAvailable) { this.deliveryAvailable = deliveryAvailable; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getFacilityType() { return facilityType; }
    public void setFacilityType(String facilityType) { this.facilityType = facilityType; }

    public String getProviderPlaceId() { return providerPlaceId; }
    public void setProviderPlaceId(String providerPlaceId) { this.providerPlaceId = providerPlaceId; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public LocalDateTime getLastVerifiedAt() { return lastVerifiedAt; }
    public void setLastVerifiedAt(LocalDateTime lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
