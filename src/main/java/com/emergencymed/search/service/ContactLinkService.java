package com.emergencymed.search.service;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.contract.MedicineLookup;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.common.dto.MedicineDto;
import com.emergencymed.common.dto.PharmacyDto;
import com.emergencymed.common.enums.Role;
import com.emergencymed.common.exception.ForbiddenException;
import com.emergencymed.common.exception.PharmacyNotApprovedException;
import com.emergencymed.common.exception.ResourceNotFoundException;
import com.emergencymed.common.exception.UnauthorizedException;
import com.emergencymed.common.util.PhoneUtils;
import com.emergencymed.search.dto.ContactInfo;
import com.emergencymed.search.entity.ContactLog;
import com.emergencymed.search.repository.ContactLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ContactLinkService {

    private final PharmacyLookup pharmacyLookup;
    private final MedicineLookup medicineLookup;
    private final ContactLogRepository contactLogRepository;
    private final CurrentUserProvider currentUserProvider;

    @Autowired
    public ContactLinkService(PharmacyLookup pharmacyLookup,
                              MedicineLookup medicineLookup,
                              ContactLogRepository contactLogRepository,
                              CurrentUserProvider currentUserProvider) {
        this.pharmacyLookup = pharmacyLookup;
        this.medicineLookup = medicineLookup;
        this.contactLogRepository = contactLogRepository;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Generates Call and WhatsApp contact links.
     * Enforces authentication: phone numbers are never exposed to unauthenticated users.
     */
    public ContactInfo getContactLinks(Long pharmacyId, Long medicineId, Integer quantity) {
        if (!currentUserProvider.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required to access pharmacy contact details");
        }

        Role role = currentUserProvider.role();
        if (role != Role.PATIENT && role != Role.HOSPITAL && role != Role.ADMIN) {
            throw new ForbiddenException("Your role does not permit accessing pharmacy contact details");
        }

        PharmacyDto pharmacy = pharmacyLookup.findApproved(pharmacyId).orElse(null);
        if (pharmacy == null) {
            throw new ResourceNotFoundException("Approved pharmacy not found for ID: " + pharmacyId);
        }

        String medicineName = null;
        if (medicineId != null) {
            MedicineDto med = medicineLookup.findById(medicineId).orElse(null);
            if (med != null) {
                medicineName = med.getName();
            }
        }

        String callUrl = PhoneUtils.formatTelUrl(pharmacy.getPhoneNumber());
        String whatsappUrl = PhoneUtils.formatWhatsappUrl(pharmacy.getPhoneNumber(), medicineName, quantity);

        return new ContactInfo(callUrl, whatsappUrl);
    }

    /**
     * Logs a CALL, WHATSAPP, or NAVIGATION action into contact_logs.
     */
    @Transactional
    public ContactLog logContact(Long pharmacyId, Long medicineId, String contactType) {
        Long userId = currentUserProvider.userId();
        if (userId == null) {
            userId = 1L; // fallback guest identifier if needed
        }

        ContactLog log = new ContactLog(
                userId,
                pharmacyId,
                medicineId,
                contactType.toUpperCase(),
                LocalDateTime.now()
        );

        return contactLogRepository.save(log);
    }
}
