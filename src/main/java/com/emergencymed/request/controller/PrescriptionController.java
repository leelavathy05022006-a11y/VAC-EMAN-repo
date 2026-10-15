package com.emergencymed.request.controller;

import com.emergencymed.request.dto.ApiResponse;
import com.emergencymed.request.dto.VerifyPrescriptionDto;
import com.emergencymed.request.entity.Prescription;
import com.emergencymed.request.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadPrescription(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "doctorName", required = false) String doctorName) {
        Long prescriptionId = prescriptionService.uploadPrescription(file, doctorName);
        return ResponseEntity.ok(ApiResponse.ok("Prescription uploaded successfully", Map.of("prescriptionId", prescriptionId)));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getPrescriptionFile(@PathVariable("id") Long id) {
        Prescription prescription = prescriptionService.getPrescription(id);
        Resource resource = prescriptionService.loadFile(id);

        String contentType = prescription.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + prescription.getOriginalName() + "\"")
                .body(resource);
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<Prescription>> verifyPrescription(
            @PathVariable("id") Long id,
            @Valid @RequestBody VerifyPrescriptionDto dto) {
        Prescription updated = prescriptionService.verifyPrescription(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Prescription verification status updated", updated));
    }
}
