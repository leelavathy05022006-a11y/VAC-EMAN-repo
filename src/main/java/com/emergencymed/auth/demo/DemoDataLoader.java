package com.emergencymed.auth.demo;

import com.emergencymed.auth.entity.*;
import com.emergencymed.auth.repository.*;
import com.emergencymed.common.enums.ApprovalStatus;
import com.emergencymed.common.enums.Role;
import com.emergencymed.request.entity.DeliveryAvailability;
import com.emergencymed.request.repository.DeliveryAvailabilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("!prod")
@Order(100)
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final HospitalProfileRepository hospitalProfileRepository;
    private final PharmacyRepository pharmacyRepository;
    private final DeliveryPartnerProfileRepository deliveryPartnerProfileRepository;
    private final DeliveryAvailabilityRepository deliveryAvailabilityRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataLoader(UserRepository userRepository,
                          PatientProfileRepository patientProfileRepository,
                          HospitalProfileRepository hospitalProfileRepository,
                          PharmacyRepository pharmacyRepository,
                          DeliveryPartnerProfileRepository deliveryPartnerProfileRepository,
                          DeliveryAvailabilityRepository deliveryAvailabilityRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.hospitalProfileRepository = hospitalProfileRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.deliveryPartnerProfileRepository = deliveryPartnerProfileRepository;
        this.deliveryAvailabilityRepository = deliveryAvailabilityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized with users. Skipping demo data loader.");
            return;
        }

        log.info("Seeding demo data for Emergency Medicine Availability Network (Module 1)...");

        // 1. Patient Account (ID 1)
        User patientUser = new User(
                "Rajesh Kumar",
                "patient@demo.in",
                "9876543211",
                passwordEncoder.encode("Patient@123"),
                Role.PATIENT
        );
        patientUser = userRepository.save(patientUser);

        PatientProfile patientProfile = new PatientProfile(patientUser);
        patientProfile.setDateOfBirth(LocalDate.of(1995, 6, 15));
        patientProfile.setGender("Male");
        patientProfile.setBloodGroup("O+");
        patientProfile.setAddress("42, Gandhi Road, Adyar");
        patientProfile.setCity("Chennai");
        patientProfile.setPincode("600020");
        patientProfile.setLatitude(13.0033);
        patientProfile.setLongitude(80.2550);
        patientProfile.setEmergencyContactName("Priya Kumar");
        patientProfile.setEmergencyContactPhone("9876543290");
        patientProfileRepository.save(patientProfile);

        // 2. Hospital Account (ID 2)
        User hospitalUser = new User(
                "Apollo Speciality Hospital Admin",
                "hospital@demo.in",
                "9876543212",
                passwordEncoder.encode("Hospital@123"),
                Role.HOSPITAL
        );
        hospitalUser = userRepository.save(hospitalUser);

        HospitalProfile hospitalProfile = new HospitalProfile(
                hospitalUser,
                "Apollo Speciality Hospital",
                "HOSP-TN-2024-001"
        );
        hospitalProfile.setHospitalType("Multi-Speciality");
        hospitalProfile.setEmergencyPhone("9876543299");
        hospitalProfile.setAddress("21, Greams Lane, Thousand Lights");
        hospitalProfile.setCity("Chennai");
        hospitalProfile.setPincode("600006");
        hospitalProfile.setLatitude(13.0569);
        hospitalProfile.setLongitude(80.2514);
        hospitalProfileRepository.save(hospitalProfile);

        // 3. Pharmacy 1: MedPlus Adyar (APPROVED) (User ID 3)
        User pharm1User = new User(
                "MedPlus Pharmacy Adyar Manager",
                "pharmacy@demo.in",
                "9876543213",
                passwordEncoder.encode("Pharmacy@123"),
                Role.PHARMACY
        );
        pharm1User = userRepository.save(pharm1User);

        Pharmacy pharmacy1 = new Pharmacy(pharm1User, "MedPlus Pharmacy Adyar", "DL-TN-CHE-2024-001", "15, Sardar Patel Road, Adyar", 13.0012, 80.2565);
        pharmacy1.setGstNo("33AAAAA0000A1Z5");
        pharmacy1.setCity("Chennai");
        pharmacy1.setPincode("600020");
        pharmacy1.setApprovalStatus(ApprovalStatus.APPROVED);
        pharmacy1.setDemo(true);
        pharmacy1.setDeliveryAvailable(true);
        pharmacy1.setApprovedAt(LocalDateTime.now().minusDays(10));
        pharmacyRepository.save(pharmacy1);

        // 4. Delivery Partner 1: Suresh (Verified) (User ID 4)
        User delivery1User = new User(
                "Suresh Delivery",
                "delivery@demo.in",
                "9876543218",
                passwordEncoder.encode("Delivery@123"),
                Role.DELIVERY
        );
        delivery1User = userRepository.save(delivery1User);

        DeliveryPartnerProfile delivery1 = new DeliveryPartnerProfile(delivery1User);
        delivery1.setVehicleType("Motorcycle");
        delivery1.setVehicleNumber("TN09AB1234");
        delivery1.setDrivingLicenseNo("DL-TN-2020-0012345");
        delivery1.setServiceCity("Chennai");
        delivery1.setVerified(true);
        deliveryPartnerProfileRepository.save(delivery1);

        // Mark partner 4 available for immediate order dispatch
        DeliveryAvailability da1 = new DeliveryAvailability(
                delivery1User.getId(),
                true,
                BigDecimal.valueOf(13.004000),
                BigDecimal.valueOf(80.256000),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        deliveryAvailabilityRepository.save(da1);

        // 5. Admin Account (User ID 5)
        User admin = new User(
                "System Administrator",
                "admin@demo.in",
                "9876543210",
                passwordEncoder.encode("Admin@123"),
                Role.ADMIN
        );
        admin = userRepository.save(admin);
        pharmacy1.setApprovedByUser(admin);
        pharmacyRepository.save(pharmacy1);

        // 6. Additional Pharmacies
        // Pharmacy 2: Apollo Pharmacy T. Nagar (APPROVED)
        createPharmacy(
                "Apollo Pharmacy T. Nagar",
                "pharmacy2@emergencymed.org",
                "9876543214",
                "DL-TN-CHE-2024-002",
                "33BBBBB0000B1Z5",
                "88, Usman Road, T. Nagar",
                "Chennai",
                "600017",
                13.0418,
                80.2341,
                ApprovalStatus.APPROVED,
                null,
                admin
        );

        // Pharmacy 3: HealthFirst Pharmacy Velachery (APPROVED)
        createPharmacy(
                "HealthFirst Pharmacy Velachery",
                "pharmacy3@emergencymed.org",
                "9876543215",
                "DL-TN-CHE-2024-003",
                "33CCCCC0000C1Z5",
                "104, 100 Feet Bypass Road, Velachery",
                "Chennai",
                "600042",
                12.9815,
                80.2180,
                ApprovalStatus.APPROVED,
                null,
                admin
        );

        // Pharmacy 4: City Care Meds Guindy (PENDING)
        createPharmacy(
                "City Care Meds Guindy",
                "pharmacy4@emergencymed.org",
                "9876543216",
                "DL-TN-CHE-2024-004",
                "33DDDDD0000D1Z5",
                "5, GST Road, Guindy",
                "Chennai",
                "600032",
                13.0067,
                80.2025,
                ApprovalStatus.PENDING,
                null,
                null
        );

        // Pharmacy 5: Express Meds Anna Nagar (SUSPENDED)
        createPharmacy(
                "Express Meds Anna Nagar",
                "pharmacy5@emergencymed.org",
                "9876543217",
                "DL-TN-CHE-2024-005",
                "33EEEEE0000E1Z5",
                "22, 2nd Avenue, Anna Nagar",
                "Chennai",
                "600040",
                13.0850,
                80.2101,
                ApprovalStatus.SUSPENDED,
                "Licence verification pending annual re-inspection",
                null
        );

        // 7. Delivery Partner 2: Ramesh (Unverified)
        User delivery2User = new User(
                "Ramesh Delivery",
                "delivery2@emergencymed.org",
                "9876543219",
                passwordEncoder.encode("Delivery@123"),
                Role.DELIVERY
        );
        delivery2User = userRepository.save(delivery2User);

        DeliveryPartnerProfile delivery2 = new DeliveryPartnerProfile(delivery2User);
        delivery2.setVehicleType("Electric Scooter");
        delivery2.setVehicleNumber("TN07CD5678");
        delivery2.setDrivingLicenseNo("DL-TN-2021-0067890");
        delivery2.setServiceCity("Chennai");
        delivery2.setVerified(false);
        deliveryPartnerProfileRepository.save(delivery2);

        DeliveryAvailability da2 = new DeliveryAvailability(
                delivery2User.getId(),
                true,
                BigDecimal.valueOf(13.010000),
                BigDecimal.valueOf(80.250000),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        deliveryAvailabilityRepository.save(da2);

    }

    private void createPharmacy(String name, String email, String phone, String licenseNo, String gstNo,
                                String address, String city, String pincode, double lat, double lng,
                                ApprovalStatus status, String rejectionReason, User approvedBy) {
        User user = new User(
                name + " Manager",
                email,
                phone,
                passwordEncoder.encode("Pharmacy@123"),
                Role.PHARMACY
        );
        user = userRepository.save(user);

        Pharmacy pharmacy = new Pharmacy(user, name, licenseNo, address, lat, lng);
        pharmacy.setGstNo(gstNo);
        pharmacy.setCity(city);
        pharmacy.setPincode(pincode);
        pharmacy.setApprovalStatus(status);
        pharmacy.setRejectionReason(rejectionReason);
        pharmacy.setDemo(true);
        if (status == ApprovalStatus.APPROVED) {
            pharmacy.setApprovedByUser(approvedBy);
            pharmacy.setApprovedAt(LocalDateTime.now().minusDays(10));
        }
        pharmacyRepository.save(pharmacy);
    }
}
