package com.emergencymed.auth.service;

import com.emergencymed.auth.dto.UpdateLocationRequest;
import com.emergencymed.auth.dto.UpdateProfileRequest;
import com.emergencymed.auth.dto.UserProfileResponse;
import com.emergencymed.auth.entity.*;
import com.emergencymed.auth.repository.*;
import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.exception.BusinessException;
import com.emergencymed.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final HospitalProfileRepository hospitalProfileRepository;
    private final PharmacyRepository pharmacyRepository;
    private final DeliveryPartnerProfileRepository deliveryPartnerProfileRepository;
    private final CurrentUserProvider currentUserProvider;

    public ProfileService(UserRepository userRepository,
                          PatientProfileRepository patientProfileRepository,
                          HospitalProfileRepository hospitalProfileRepository,
                          PharmacyRepository pharmacyRepository,
                          DeliveryPartnerProfileRepository deliveryPartnerProfileRepository,
                          CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.hospitalProfileRepository = hospitalProfileRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.deliveryPartnerProfileRepository = deliveryPartnerProfileRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile() {
        Long userId = currentUserProvider.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        UserProfileResponse resp = new UserProfileResponse();
        resp.setId(user.getId());
        resp.setFullName(user.getFullName());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setRole(user.getRole());
        resp.setActive(user.isActive());
        resp.setLastLoginAt(user.getLastLoginAt());
        resp.setCreatedAt(user.getCreatedAt());

        switch (user.getRole()) {
            case PATIENT -> patientProfileRepository.findByUserId(userId).ifPresent(p -> {
                resp.setDateOfBirth(p.getDateOfBirth());
                resp.setGender(p.getGender());
                resp.setBloodGroup(p.getBloodGroup());
                resp.setAddress(p.getAddress());
                resp.setCity(p.getCity());
                resp.setPincode(p.getPincode());
                resp.setLatitude(p.getLatitude());
                resp.setLongitude(p.getLongitude());
                resp.setEmergencyContactName(p.getEmergencyContactName());
                resp.setEmergencyContactPhone(p.getEmergencyContactPhone());
            });
            case HOSPITAL -> hospitalProfileRepository.findByUserId(userId).ifPresent(h -> {
                resp.setHospitalName(h.getHospitalName());
                resp.setRegistrationNo(h.getRegistrationNo());
                resp.setHospitalType(h.getHospitalType());
                resp.setEmergencyPhone(h.getEmergencyPhone());
                resp.setAddress(h.getAddress());
                resp.setCity(h.getCity());
                resp.setPincode(h.getPincode());
                resp.setLatitude(h.getLatitude());
                resp.setLongitude(h.getLongitude());
            });
            case PHARMACY -> pharmacyRepository.findByUserId(userId).ifPresent(p -> {
                resp.setPharmacyId(p.getId());
                resp.setPharmacyName(p.getName());
                resp.setLicenseNo(p.getLicenseNo());
                resp.setGstNo(p.getGstNo());
                resp.setAddress(p.getAddress());
                resp.setCity(p.getCity());
                resp.setPincode(p.getPincode());
                resp.setLatitude(p.getLatitude());
                resp.setLongitude(p.getLongitude());
                resp.setApprovalStatus(p.getApprovalStatus());
                resp.setRejectionReason(p.getRejectionReason());
            });
            case DELIVERY -> deliveryPartnerProfileRepository.findByUserId(userId).ifPresent(d -> {
                resp.setVehicleType(d.getVehicleType());
                resp.setVehicleNumber(d.getVehicleNumber());
                resp.setDrivingLicenseNo(d.getDrivingLicenseNo());
                resp.setServiceCity(d.getServiceCity());
                resp.setVerified(d.isVerified());
            });
            case ADMIN -> {}
        }

        return resp;
    }

    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest req) {
        Long userId = currentUserProvider.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        if (req.fullName() != null && !req.fullName().isBlank()) {
            user.setFullName(req.fullName().trim());
        }
        if (req.phone() != null && !req.phone().isBlank()) {
            user.setPhone(req.phone().trim());
        }
        userRepository.save(user);

        switch (user.getRole()) {
            case PATIENT -> patientProfileRepository.findByUserId(userId).ifPresent(p -> {
                if (req.dateOfBirth() != null && !req.dateOfBirth().isBlank()) {
                    p.setDateOfBirth(LocalDate.parse(req.dateOfBirth().trim()));
                }
                if (req.gender() != null) p.setGender(req.gender());
                if (req.bloodGroup() != null) p.setBloodGroup(req.bloodGroup());
                if (req.emergencyContactName() != null) p.setEmergencyContactName(req.emergencyContactName());
                if (req.emergencyContactPhone() != null) p.setEmergencyContactPhone(req.emergencyContactPhone());
                patientProfileRepository.save(p);
            });
            case HOSPITAL -> hospitalProfileRepository.findByUserId(userId).ifPresent(h -> {
                if (req.hospitalName() != null && !req.hospitalName().isBlank()) {
                    h.setHospitalName(req.hospitalName().trim());
                }
                if (req.hospitalType() != null) h.setHospitalType(req.hospitalType());
                if (req.emergencyPhone() != null) h.setEmergencyPhone(req.emergencyPhone());
                hospitalProfileRepository.save(h);
            });
            case DELIVERY -> deliveryPartnerProfileRepository.findByUserId(userId).ifPresent(d -> {
                if (req.vehicleType() != null) d.setVehicleType(req.vehicleType());
                if (req.vehicleNumber() != null) d.setVehicleNumber(req.vehicleNumber());
                if (req.drivingLicenseNo() != null) d.setDrivingLicenseNo(req.drivingLicenseNo());
                if (req.serviceCity() != null) d.setServiceCity(req.serviceCity());
                deliveryPartnerProfileRepository.save(d);
            });
            case PHARMACY, ADMIN -> {}
        }

        return getUserProfile();
    }

    @Transactional
    public UserProfileResponse updateLocation(UpdateLocationRequest req) {
        Long userId = currentUserProvider.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        switch (user.getRole()) {
            case PATIENT -> patientProfileRepository.findByUserId(userId).ifPresent(p -> {
                if (req.address() != null) p.setAddress(req.address().trim());
                if (req.city() != null) p.setCity(req.city().trim());
                if (req.pincode() != null) p.setPincode(req.pincode().trim());
                if (req.latitude() != null) p.setLatitude(req.latitude());
                if (req.longitude() != null) p.setLongitude(req.longitude());
                patientProfileRepository.save(p);
            });
            case HOSPITAL -> hospitalProfileRepository.findByUserId(userId).ifPresent(h -> {
                if (req.address() != null) h.setAddress(req.address().trim());
                if (req.city() != null) h.setCity(req.city().trim());
                if (req.pincode() != null) h.setPincode(req.pincode().trim());
                if (req.latitude() != null) h.setLatitude(req.latitude());
                if (req.longitude() != null) h.setLongitude(req.longitude());
                hospitalProfileRepository.save(h);
            });
            case PHARMACY -> pharmacyRepository.findByUserId(userId).ifPresent(p -> {
                if (req.address() != null) p.setAddress(req.address().trim());
                if (req.city() != null) p.setCity(req.city().trim());
                if (req.pincode() != null) p.setPincode(req.pincode().trim());
                if (req.latitude() != null) p.setLatitude(req.latitude());
                if (req.longitude() != null) p.setLongitude(req.longitude());
                pharmacyRepository.save(p);
            });
            case DELIVERY -> deliveryPartnerProfileRepository.findByUserId(userId).ifPresent(d -> {
                if (req.city() != null) d.setServiceCity(req.city().trim());
                deliveryPartnerProfileRepository.save(d);
            });
            case ADMIN -> {}
        }

        return getUserProfile();
    }
}
