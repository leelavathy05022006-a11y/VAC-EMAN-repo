package com.emergencymed.search.controller;

import com.emergencymed.common.dto.ApiResponse;
import com.emergencymed.search.dto.ContactInfo;
import com.emergencymed.search.dto.ContactLogRequest;
import com.emergencymed.search.entity.ContactLog;
import com.emergencymed.search.service.ContactLinkService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contact")
public class ContactController {

    private final ContactLinkService contactLinkService;

    @Autowired
    public ContactController(ContactLinkService contactLinkService) {
        this.contactLinkService = contactLinkService;
    }

    /**
     * GET /api/v1/contact/{pharmacyId}/links
     * Returns Call and WhatsApp contact URLs for authenticated patients, hospitals, or admins.
     */
    @GetMapping("/{pharmacyId}/links")
    public ApiResponse<ContactInfo> getContactLinks(
            @PathVariable("pharmacyId") Long pharmacyId,
            @RequestParam(value = "medicineId", required = false) Long medicineId,
            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity) {

        ContactInfo contactInfo = contactLinkService.getContactLinks(pharmacyId, medicineId, quantity);
        return ApiResponse.ok("Contact links generated successfully", contactInfo);
    }

    /**
     * POST /api/v1/contact/log
     * Logs Call, WhatsApp, or Navigation interactions for tracking and auditing.
     */
    @PostMapping("/log")
    public ApiResponse<ContactLog> logContact(@Valid @RequestBody ContactLogRequest request) {
        ContactLog log = contactLinkService.logContact(
                request.getPharmacyId(),
                request.getMedicineId(),
                request.getContactType()
        );
        return ApiResponse.ok("Contact interaction logged successfully", log);
    }
}
