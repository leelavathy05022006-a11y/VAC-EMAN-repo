package com.emergencymed.auth;

import com.emergencymed.auth.dto.LoginRequest;
import com.emergencymed.auth.dto.LoginResponse;
import com.emergencymed.auth.dto.RegisterPatientRequest;
import com.emergencymed.auth.entity.Pharmacy;
import com.emergencymed.auth.entity.User;
import com.emergencymed.auth.repository.*;
import com.emergencymed.auth.security.CustomUserDetails;
import com.emergencymed.auth.service.AuthService;
import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.enums.ApprovalStatus;
import com.emergencymed.common.enums.Role;
import com.emergencymed.common.exception.BusinessException;
import com.emergencymed.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthenticationTest {

    private UserRepository userRepository;
    private PatientProfileRepository patientProfileRepository;
    private HospitalProfileRepository hospitalProfileRepository;
    private PharmacyRepository pharmacyRepository;
    private DeliveryPartnerProfileRepository deliveryPartnerProfileRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private CurrentUserProvider currentUserProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        patientProfileRepository = mock(PatientProfileRepository.class);
        hospitalProfileRepository = mock(HospitalProfileRepository.class);
        pharmacyRepository = mock(PharmacyRepository.class);
        deliveryPartnerProfileRepository = mock(DeliveryPartnerProfileRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        currentUserProvider = mock(CurrentUserProvider.class);

        authService = new AuthService(
                userRepository,
                patientProfileRepository,
                hospitalProfileRepository,
                pharmacyRepository,
                deliveryPartnerProfileRepository,
                passwordEncoder,
                authenticationManager,
                currentUserProvider
        );
    }

    @Test
    @DisplayName("Successful patient registration hashes password and returns user ID")
    void testPatientRegistrationSuccess() {
        RegisterPatientRequest req = new RegisterPatientRequest(
                "Asha Raman",
                "asha@example.com",
                "9840123456",
                "Password@123",
                "Password@123",
                "1990-05-15",
                "FEMALE",
                "O_POSITIVE",
                "12 Gandhi Road",
                "Chennai",
                "600020",
                13.0050,
                80.2550,
                "Raman",
                "9840123450"
        );

        when(userRepository.existsByEmail("asha@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(101L);
            return u;
        });

        Long userId = authService.registerPatient(req);

        assertEquals(101L, userId);
        verify(userRepository, times(1)).save(any(User.class));
        verify(patientProfileRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Duplicate email throws BusinessException with DUPLICATE_RESOURCE code")
    void testDuplicateEmailRegistrationThrows() {
        RegisterPatientRequest req = new RegisterPatientRequest(
                "Asha Raman",
                "duplicate@example.com",
                "9840123456",
                "Password@123",
                "Password@123",
                null, null, null, null, null, null, null, null, null, null
        );

        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            authService.registerPatient(req);
        });

        assertEquals(ErrorCode.DUPLICATE_RESOURCE, ex.getErrorCode());
    }

    @Test
    @DisplayName("Mismatched password confirmation throws VALIDATION_ERROR")
    void testMismatchedPasswordThrows() {
        RegisterPatientRequest req = new RegisterPatientRequest(
                "Asha Raman",
                "asha@example.com",
                "9840123456",
                "Password@123",
                "DifferentPassword",
                null, null, null, null, null, null, null, null, null, null
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            authService.registerPatient(req);
        });

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
    }

    @Test
    @DisplayName("Successful login returns authenticated session details and redirect URL")
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest("asha@example.com", "Password@123");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession(true)).thenReturn(session);

        User user = new User("Asha Raman", "asha@example.com", "9840123456", "$2a$10$hash", Role.PATIENT);
        user.setId(101L);

        CustomUserDetails userDetails = new CustomUserDetails(user, null);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));

        LoginResponse loginResp = authService.login(req, request, response);

        assertNotNull(loginResp);
        assertEquals(101L, loginResp.userId());
        assertEquals("asha@example.com", loginResp.email());
        assertEquals(Role.PATIENT, loginResp.role());
        assertEquals("/dashboard/patient.html", loginResp.redirectUrl());
    }

    @Test
    @DisplayName("Pending pharmacy login returns PENDING approval status and pharmacy dashboard redirect")
    void testPendingPharmacyLogin() {
        LoginRequest req = new LoginRequest("pharmacy@example.com", "Password@123");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getSession(true)).thenReturn(session);

        User user = new User("Apollo T. Nagar", "pharmacy@example.com", "9840123456", "$2a$10$hash", Role.PHARMACY);
        user.setId(201L);

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(501L);
        pharmacy.setUser(user);
        pharmacy.setApprovalStatus(ApprovalStatus.PENDING);

        CustomUserDetails userDetails = new CustomUserDetails(user, 501L);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findById(201L)).thenReturn(Optional.of(user));
        when(pharmacyRepository.findByUserId(201L)).thenReturn(Optional.of(pharmacy));

        LoginResponse loginResp = authService.login(req, request, response);

        assertNotNull(loginResp);
        assertEquals(ApprovalStatus.PENDING, loginResp.approvalStatus());
        assertEquals("/dashboard/pharmacy.html", loginResp.redirectUrl());
    }
}
