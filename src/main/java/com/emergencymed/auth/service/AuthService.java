package com.emergencymed.auth.service;

import com.emergencymed.auth.dto.*;
import com.emergencymed.auth.entity.*;
import com.emergencymed.auth.repository.*;
import com.emergencymed.auth.security.CustomUserDetails;
import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.enums.ApprovalStatus;
import com.emergencymed.common.enums.Role;
import com.emergencymed.common.exception.BusinessException;
import com.emergencymed.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final HospitalProfileRepository hospitalProfileRepository;
    private final PharmacyRepository pharmacyRepository;
    private final DeliveryPartnerProfileRepository deliveryPartnerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CurrentUserProvider currentUserProvider;

    public AuthService(UserRepository userRepository,
                       PatientProfileRepository patientProfileRepository,
                       HospitalProfileRepository hospitalProfileRepository,
                       PharmacyRepository pharmacyRepository,
                       DeliveryPartnerProfileRepository deliveryPartnerProfileRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.hospitalProfileRepository = hospitalProfileRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.deliveryPartnerProfileRepository = deliveryPartnerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public Long registerPatient(RegisterPatientRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "confirmPassword", "Passwords do not match");
        }
        String email = req.email().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "email", "Email address already registered");
        }

        User user = new User(
                req.fullName().trim(),
                email,
                req.phone().trim(),
                passwordEncoder.encode(req.password()),
                Role.PATIENT
        );
        user = userRepository.save(user);

        PatientProfile profile = new PatientProfile(user);
        if (req.dateOfBirth() != null && !req.dateOfBirth().isBlank()) {
            profile.setDateOfBirth(LocalDate.parse(req.dateOfBirth().trim()));
        }
        profile.setGender(req.gender());
        profile.setBloodGroup(req.bloodGroup());
        profile.setAddress(req.address());
        profile.setCity(req.city());
        profile.setPincode(req.pincode());
        profile.setLatitude(req.latitude());
        profile.setLongitude(req.longitude());
        profile.setEmergencyContactName(req.emergencyContactName());
        profile.setEmergencyContactPhone(req.emergencyContactPhone());

        patientProfileRepository.save(profile);
        return user.getId();
    }

    @Transactional
    public Long registerHospital(RegisterHospitalRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "confirmPassword", "Passwords do not match");
        }
        String email = req.email().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "email", "Email address already registered");
        }
        if (hospitalProfileRepository.existsByRegistrationNo(req.registrationNo().trim())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "registrationNo", "Hospital registration number already registered");
        }

        User user = new User(
                req.hospitalName().trim(),
                email,
                req.phone().trim(),
                passwordEncoder.encode(req.password()),
                Role.HOSPITAL
        );
        user = userRepository.save(user);

        HospitalProfile profile = new HospitalProfile(user, req.hospitalName().trim(), req.registrationNo().trim());
        profile.setHospitalType(req.hospitalType());
        profile.setEmergencyPhone(req.emergencyPhone());
        profile.setAddress(req.address());
        profile.setCity(req.city());
        profile.setPincode(req.pincode());
        profile.setLatitude(req.latitude());
        profile.setLongitude(req.longitude());

        hospitalProfileRepository.save(profile);
        return user.getId();
    }

    @Transactional
    public Long registerPharmacy(RegisterPharmacyRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "confirmPassword", "Passwords do not match");
        }
        String email = req.email().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "email", "Email address already registered");
        }
        if (pharmacyRepository.existsByLicenseNo(req.licenseNo().trim())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "licenseNo", "Pharmacy licence number already registered");
        }

        User user = new User(
                req.contactPersonName().trim(),
                email,
                req.phone().trim(),
                passwordEncoder.encode(req.password()),
                Role.PHARMACY
        );
        user = userRepository.save(user);

        Pharmacy pharmacy = new Pharmacy(
                user,
                req.pharmacyName().trim(),
                req.licenseNo().trim(),
                req.address().trim(),
                req.latitude(),
                req.longitude()
        );
        pharmacy.setGstNo(req.gstNo());
        pharmacy.setCity(req.city());
        pharmacy.setPincode(req.pincode());
        pharmacy.setApprovalStatus(ApprovalStatus.PENDING);

        pharmacyRepository.save(pharmacy);
        return user.getId();
    }

    @Transactional
    public Long registerDeliveryPartner(RegisterDeliveryRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "confirmPassword", "Passwords do not match");
        }
        String email = req.email().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "email", "Email address already registered");
        }

        User user = new User(
                req.fullName().trim(),
                email,
                req.phone().trim(),
                passwordEncoder.encode(req.password()),
                Role.DELIVERY
        );
        user = userRepository.save(user);

        DeliveryPartnerProfile profile = new DeliveryPartnerProfile(user);
        profile.setVehicleType(req.vehicleType());
        profile.setVehicleNumber(req.vehicleNumber());
        profile.setDrivingLicenseNo(req.drivingLicenseNo().trim());
        profile.setServiceCity(req.city());
        profile.setVerified(false);

        deliveryPartnerProfileRepository.save(profile);
        return user.getId();
    }

    @Transactional
    public LoginResponse login(LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email().toLowerCase().trim(), req.password())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            ApprovalStatus approvalStatus = null;
            if (user.getRole() == Role.PHARMACY) {
                approvalStatus = pharmacyRepository.findByUserId(user.getId())
                        .map(Pharmacy::getApprovalStatus)
                        .orElse(ApprovalStatus.PENDING);
            }

            String redirectUrl = determineRedirectUrl(user.getRole());

            return new LoginResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole(),
                    approvalStatus,
                    redirectUrl
            );

        } catch (DisabledException ex) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Account is deactivated. Please contact support.");
        } catch (BadCredentialsException ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid email or password");
        }
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "confirmPassword", "New password and confirmation do not match");
        }

        Long userId = currentUserProvider.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(req.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "oldPassword", "Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse getCurrentUserSummary() {
        if (!currentUserProvider.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not logged in");
        }
        Long userId = currentUserProvider.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        ApprovalStatus approvalStatus = null;
        if (user.getRole() == Role.PHARMACY) {
            approvalStatus = pharmacyRepository.findByUserId(user.getId())
                    .map(Pharmacy::getApprovalStatus)
                    .orElse(ApprovalStatus.PENDING);
        }

        return new LoginResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                approvalStatus,
                determineRedirectUrl(user.getRole())
        );
    }

    private String determineRedirectUrl(Role role) {
        return switch (role) {
            case ADMIN -> "/dashboard/admin.html";
            case PATIENT -> "/dashboard/patient.html";
            case HOSPITAL -> "/dashboard/hospital.html";
            case PHARMACY -> "/dashboard/pharmacy.html";
            case DELIVERY -> "/dashboard/delivery.html";
        };
    }
}
