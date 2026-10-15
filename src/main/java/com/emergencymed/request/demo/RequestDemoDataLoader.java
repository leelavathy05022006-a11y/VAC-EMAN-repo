package com.emergencymed.request.demo;

import com.emergencymed.common.enums.Role;
import com.emergencymed.request.dto.CreateRequestDto;
import com.emergencymed.request.dto.RequestDetailResponse;
import com.emergencymed.request.dto.RequestItemDto;
import com.emergencymed.request.dto.VerifyPrescriptionDto;
import com.emergencymed.auth.security.CustomUserDetails;
import com.emergencymed.request.repository.EmergencyRequestRepository;
import com.emergencymed.request.service.DeliveryAssignmentService;
import com.emergencymed.request.service.PrescriptionService;
import com.emergencymed.request.service.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

@Component
@Order(400)
@Profile("!prod")
public class RequestDemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RequestDemoDataLoader.class);

    private final RequestService requestService;
    private final PrescriptionService prescriptionService;
    private final DeliveryAssignmentService deliveryAssignmentService;
    private final EmergencyRequestRepository requestRepository;

    public RequestDemoDataLoader(
            RequestService requestService,
            PrescriptionService prescriptionService,
            DeliveryAssignmentService deliveryAssignmentService,
            EmergencyRequestRepository requestRepository) {
        this.requestService = requestService;
        this.prescriptionService = prescriptionService;
        this.deliveryAssignmentService = deliveryAssignmentService;
        this.requestRepository = requestRepository;
    }

    public static class DemoMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public DemoMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }

    private void setSecurityUser(Long userId, Role role, Long pharmacyId) {
        CustomUserDetails principal = new CustomUserDetails(userId, "user" + userId + "@example.com", role, pharmacyId);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (requestRepository.count() > 0) {
            log.info("Emergency requests already present ({}), skipping demo data loader", requestRepository.count());
            return;
        }

        log.info("Initializing RequestDemoDataLoader (Order 400)...");

        try {
            // Setup demo prescription file
            byte[] dummyPdfContent = "%PDF-1.4 sample emergency prescription file content".getBytes();
            MultipartFile samplePdf = new DemoMultipartFile(
                    "file", "emergency_rx.pdf", "application/pdf", dummyPdfContent
            );

            // 1. Sample PENDING request with prescription
            setSecurityUser(1L, Role.PATIENT, null);
            Long rxId1 = prescriptionService.uploadPrescription(samplePdf, "Dr. K. S. Rao, MD");

            CreateRequestDto pendingDto = new CreateRequestDto();
            pendingDto.setPharmacyId(101L);
            pendingDto.setFulfilmentType("DELIVERY");
            pendingDto.setPriority("CRITICAL");
            pendingDto.setPatientName("Asha Raman");
            pendingDto.setPatientPhone("9000000011");
            pendingDto.setDeliveryAddress("Flat 4B, Sample Apartments, Adyar, Chennai");
            pendingDto.setDeliveryLat(BigDecimal.valueOf(13.0050));
            pendingDto.setDeliveryLng(BigDecimal.valueOf(80.2550));
            pendingDto.setPrescriptionId(rxId1);
            pendingDto.setNotes("Patient is in severe respiratory distress, urgent delivery requested");
            pendingDto.setItems(List.of(
                    new RequestItemDto(1L, 1),
                    new RequestItemDto(4L, 1)
            ));
            RequestDetailResponse req1 = requestService.createRequest(pendingDto);
            log.info("Created Demo Request #1 (PENDING): {}", req1.getReferenceCode());

            // 2. Sample READY_FOR_PICKUP request
            setSecurityUser(2L, Role.HOSPITAL, null);
            Long rxId2 = prescriptionService.uploadPrescription(samplePdf, "Dr. Sunita Sen");
            CreateRequestDto pickupDto = new CreateRequestDto();
            pickupDto.setPharmacyId(101L);
            pickupDto.setFulfilmentType("SELF_PICKUP");
            pickupDto.setPriority("URGENT");
            pickupDto.setPatientName("Ramesh Kumar (Ward 3)");
            pickupDto.setPatientPhone("9000000022");
            pickupDto.setDoctorName("Dr. Sunita Sen");
            pickupDto.setDoctorRegNo("MCI-88492");
            pickupDto.setPrescriptionId(rxId2);
            pickupDto.setNotes("Hospital ambulance runner will collect within 45 mins");
            pickupDto.setItems(List.of(
                    new RequestItemDto(5L, 2),
                    new RequestItemDto(1L, 1)
            ));
            RequestDetailResponse req2 = requestService.createRequest(pickupDto);

            // Pharmacy verifies prescription and accepts request #2 -> READY_FOR_PICKUP
            setSecurityUser(3L, Role.PHARMACY, 101L);
            prescriptionService.verifyPrescription(rxId2, new VerifyPrescriptionDto("VERIFIED", "Requisition verified by pharmacist"));
            requestService.acceptRequest(req2.getId());
            log.info("Created Demo Request #2 (READY_FOR_PICKUP): {}", req2.getReferenceCode());

            // 3. Sample DELIVERED request
            setSecurityUser(1L, Role.PATIENT, null);
            Long rxId3 = prescriptionService.uploadPrescription(samplePdf, "Dr. Vikram Mehta");
            CreateRequestDto deliveredDto = new CreateRequestDto();
            deliveredDto.setPharmacyId(101L);
            deliveredDto.setFulfilmentType("DELIVERY");
            deliveredDto.setPriority("URGENT");
            deliveredDto.setPatientName("Priya Sharma");
            deliveredDto.setPatientPhone("9000000011");
            deliveredDto.setDeliveryAddress("14 Beach Road, Thiruvanmiyur, Chennai");
            deliveredDto.setDeliveryLat(BigDecimal.valueOf(12.9850));
            deliveredDto.setDeliveryLng(BigDecimal.valueOf(80.2600));
            deliveredDto.setPrescriptionId(rxId3);
            deliveredDto.setItems(List.of(
                    new RequestItemDto(3L, 1),
                    new RequestItemDto(4L, 1)
            ));
            RequestDetailResponse req3 = requestService.createRequest(deliveredDto);

            // Pharmacy verifies prescription and accepts
            setSecurityUser(3L, Role.PHARMACY, 101L);
            prescriptionService.verifyPrescription(rxId3, new VerifyPrescriptionDto("VERIFIED", "Prescription verified by pharmacist"));
            requestService.acceptRequest(req3.getId());

            // Delivery Partner 4 (Rahul Rider) accepts offer, picks up, delivers
            setSecurityUser(4L, Role.DELIVERY, null);
            var offers = deliveryAssignmentService.getOpenOffersForPartner(4L);
            if (!offers.isEmpty()) {
                Long assignmentId = offers.get(0).getAssignmentId();
                deliveryAssignmentService.acceptOffer(assignmentId, 4L);
                deliveryAssignmentService.markPickedUp(req3.getId(), 4L);

                // Fetch handover code
                com.emergencymed.request.entity.EmergencyRequest r3Entity = requestRepository.findById(req3.getId()).get();
                deliveryAssignmentService.markDelivered(req3.getId(), 4L, r3Entity.getHandoverCode());
                log.info("Created Demo Request #3 (DELIVERED): {}", req3.getReferenceCode());
            }

        } catch (Exception e) {
            log.error("Failed to load demo requests", e);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
