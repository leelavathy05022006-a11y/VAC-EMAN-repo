package com.emergencymed.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hospital_profiles")
public class HospitalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "hospital_name", nullable = false, length = 150)
    private String hospitalName;

    @Column(name = "registration_no", nullable = false, unique = true, length = 50)
    private String registrationNo;

    @Column(name = "hospital_type", length = 30)
    private String hospitalType;

    @Column(name = "emergency_phone", length = 15)
    private String emergencyPhone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "city", length = 60)
    private String city;

    @Column(name = "pincode", length = 6)
    private String pincode;

    @Column(name = "latitude", columnDefinition = "DECIMAL(9,6)")
    private Double latitude;

    @Column(name = "longitude", columnDefinition = "DECIMAL(9,6)")
    private Double longitude;

    public HospitalProfile() {}

    public HospitalProfile(User user, String hospitalName, String registrationNo) {
        this.user = user;
        this.hospitalName = hospitalName;
        this.registrationNo = registrationNo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getHospitalType() {
        return hospitalType;
    }

    public void setHospitalType(String hospitalType) {
        this.hospitalType = hospitalType;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
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
}
