package com.emergencymed.request.service;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.enums.Role;
import com.emergencymed.request.dto.VerifyPrescriptionDto;
import com.emergencymed.request.entity.EmergencyRequest;
import com.emergencymed.request.entity.Prescription;
import com.emergencymed.request.exception.AccessForbiddenException;
import com.emergencymed.request.exception.ResourceNotFoundException;
import com.emergencymed.request.repository.EmergencyRequestRepository;
import com.emergencymed.request.repository.PrescriptionRepository;
import com.emergencymed.request.storage.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final EmergencyRequestRepository requestRepository;
    private final FileStorageService fileStorageService;
    private final CurrentUserProvider currentUserProvider;

    public PrescriptionService(
            PrescriptionRepository prescriptionRepository,
            EmergencyRequestRepository requestRepository,
            FileStorageService fileStorageService,
            CurrentUserProvider currentUserProvider) {
        this.prescriptionRepository = prescriptionRepository;
        this.requestRepository = requestRepository;
        this.fileStorageService = fileStorageService;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public Long uploadPrescription(MultipartFile file, String doctorName) {
        Long userId = currentUserProvider.userId();
        FileStorageService.StoredFile stored = fileStorageService.store(file);

        Prescription prescription = new Prescription();
        prescription.setUploadedByUserId(userId);
        prescription.setFileName(stored.getStorageFilename());
        prescription.setOriginalName(stored.getOriginalFilename());
        prescription.setContentType(stored.getContentType());
        prescription.setFileSize((int) stored.getSize());
        prescription.setDoctorName(doctorName);
        prescription.setVerificationStatus("PENDING");
        prescription.setCreatedAt(LocalDateTime.now());

        Prescription saved = prescriptionRepository.save(prescription);
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public Prescription getPrescription(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Resource loadFile(Long prescriptionId) {
        Prescription prescription = getPrescription(prescriptionId);
        Long currentUserId = currentUserProvider.userId();
        Role role = currentUserProvider.role();

        boolean isUploader = prescription.getUploadedByUserId().equals(currentUserId);
        boolean isAdmin = role == Role.ADMIN;
        boolean isAuthorizedPharmacy = false;

        if (prescription.getRequestId() != null) {
            EmergencyRequest request = requestRepository.findById(prescription.getRequestId()).orElse(null);
            if (request != null) {
                Long pharmacyId = currentUserProvider.pharmacyId().orElse(null);
                if (pharmacyId != null && pharmacyId.equals(request.getPharmacyId())) {
                    isAuthorizedPharmacy = true;
                }
            }
        }

        if (!isUploader && !isAdmin && !isAuthorizedPharmacy) {
            throw new AccessForbiddenException("You do not have permission to view this prescription");
        }

        return fileStorageService.loadAsResource(prescription.getFileName());
    }

    @Transactional
    public Prescription verifyPrescription(Long prescriptionId, VerifyPrescriptionDto dto) {
        Prescription prescription = getPrescription(prescriptionId);
        Role role = currentUserProvider.role();

        if (role != Role.PHARMACY && role != Role.ADMIN) {
            throw new AccessForbiddenException("Only pharmacy users can verify prescriptions");
        }

        Long pharmacyId = currentUserProvider.pharmacyId().orElse(null);
        if (prescription.getRequestId() != null) {
            EmergencyRequest request = requestRepository.findById(prescription.getRequestId()).orElse(null);
            if (request != null) {
                if (pharmacyId != null && !pharmacyId.equals(request.getPharmacyId()) && role != Role.ADMIN) {
                    throw new AccessForbiddenException("Only the assigned pharmacy can verify this prescription");
                }
                // Update request's prescriptionStatus
                request.setPrescriptionStatus(dto.getStatus());
                requestRepository.save(request);
            }
        }

        prescription.setVerificationStatus(dto.getStatus());
        prescription.setRemarks(dto.getRemarks());
        prescription.setVerifiedByUserId(currentUserProvider.userId());
        prescription.setVerifiedAt(LocalDateTime.now());

        return prescriptionRepository.save(prescription);
    }
}
