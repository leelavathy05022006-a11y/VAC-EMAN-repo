package com.emergencymed.auth.service;

import com.emergencymed.auth.dto.AdminSummaryDto;
import com.emergencymed.auth.dto.UpdatePharmacyRequest;
import com.emergencymed.auth.entity.DeliveryPartnerProfile;
import com.emergencymed.auth.entity.Pharmacy;
import com.emergencymed.auth.entity.User;
import com.emergencymed.auth.repository.DeliveryPartnerProfileRepository;
import com.emergencymed.auth.repository.PharmacyRepository;
import com.emergencymed.auth.repository.UserRepository;
import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.contract.NotificationPublisher;
import com.emergencymed.common.contract.dto.PharmacySummaryDto;
import com.emergencymed.common.enums.ApprovalStatus;
import com.emergencymed.common.enums.NotificationType;
import com.emergencymed.common.enums.Role;
import com.emergencymed.common.events.PharmacyApprovedEvent;
import com.emergencymed.common.events.PharmacyRejectedEvent;
import com.emergencymed.common.exception.BusinessException;
import com.emergencymed.common.exception.ErrorCode;
import com.emergencymed.common.util.MapLinks;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PharmacyApprovalService {

    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final DeliveryPartnerProfileRepository deliveryPartnerProfileRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationPublisher notificationPublisher;
    private final CurrentUserProvider currentUserProvider;

    public PharmacyApprovalService(PharmacyRepository pharmacyRepository,
                                   UserRepository userRepository,
                                   DeliveryPartnerProfileRepository deliveryPartnerProfileRepository,
                                   ApplicationEventPublisher eventPublisher,
                                   NotificationPublisher notificationPublisher,
                                   CurrentUserProvider currentUserProvider) {
        this.pharmacyRepository = pharmacyRepository;
        this.userRepository = userRepository;
        this.deliveryPartnerProfileRepository = deliveryPartnerProfileRepository;
        this.eventPublisher = eventPublisher;
        this.notificationPublisher = notificationPublisher;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public AdminSummaryDto getAdminSummary() {
        long totalUsers = userRepository.count();
        long patientsCount = userRepository.countByRole(Role.PATIENT);
        long hospitalsCount = userRepository.countByRole(Role.HOSPITAL);
        long pharmaciesCount = userRepository.countByRole(Role.PHARMACY);
        long pendingPharmaciesCount = pharmacyRepository.countByApprovalStatus(ApprovalStatus.PENDING);
        long approvedPharmaciesCount = pharmacyRepository.countByApprovalStatus(ApprovalStatus.APPROVED);
        long deliveryPartnersCount = userRepository.countByRole(Role.DELIVERY);
        long unverifiedDeliveryPartnersCount = deliveryPartnerProfileRepository.countByIsVerifiedFalse();

        return new AdminSummaryDto(
                totalUsers,
                patientsCount,
                hospitalsCount,
                pharmaciesCount,
                pendingPharmaciesCount,
                approvedPharmaciesCount,
                deliveryPartnersCount,
                unverifiedDeliveryPartnersCount
        );
    }

    @Transactional(readOnly = true)
    public Page<User> getUsers(Role role, Boolean active, String q, Pageable pageable) {
        return userRepository.searchUsers(role, active, q, pageable);
    }

    @Transactional
    public void updateUserStatus(Long targetUserId, boolean active) {
        Long currentAdminId = currentUserProvider.userId();
        if (targetUserId.equals(currentAdminId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "An administrator cannot deactivate their own account");
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        user.setActive(active);
        userRepository.save(user);
    }

    @Transactional
    public void verifyDeliveryPartner(Long userId) {
        DeliveryPartnerProfile profile = deliveryPartnerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Delivery partner profile not found for user ID: " + userId));

        profile.setVerified(true);
        deliveryPartnerProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public Page<PharmacySummaryDto> getPharmacies(ApprovalStatus status, String q, Pageable pageable) {
        return pharmacyRepository.searchPharmacies(status, q, pageable)
                .map(this::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public PharmacySummaryDto getPharmacy(Long id) {
        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Pharmacy not found with ID: " + id));
        return toSummaryDto(pharmacy);
    }

    @Transactional
    public PharmacySummaryDto approvePharmacy(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Pharmacy not found with ID: " + pharmacyId));

        if (pharmacy.getApprovalStatus() != ApprovalStatus.PENDING && pharmacy.getApprovalStatus() != ApprovalStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Cannot approve pharmacy with status: " + pharmacy.getApprovalStatus());
        }

        Long adminId = currentUserProvider.userId();
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Admin user not found"));

        pharmacy.setApprovalStatus(ApprovalStatus.APPROVED);
        pharmacy.setApprovedByUser(admin);
        pharmacy.setApprovedAt(LocalDateTime.now());
        pharmacy.setRejectionReason(null);
        pharmacy = pharmacyRepository.save(pharmacy);

        // Publish Spring domain event for other modules
        eventPublisher.publishEvent(new PharmacyApprovedEvent(
                pharmacy.getId(),
                pharmacy.getUser().getId(),
                pharmacy.getName(),
                pharmacy.getLicenseNo(),
                pharmacy.getLatitude(),
                pharmacy.getLongitude()
        ));

        // Call notification publisher contract
        notificationPublisher.publish(
                pharmacy.getUser().getId(),
                NotificationType.PHARMACY_APPROVED,
                "Pharmacy Approved",
                "Your pharmacy '" + pharmacy.getName() + "' has been approved by administrator and is now active.",
                null
        );

        return toSummaryDto(pharmacy);
    }

    @Transactional
    public PharmacySummaryDto rejectPharmacy(Long pharmacyId, String reason) {
        if (reason == null || reason.trim().length() < 10) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "reason", "Rejection reason must be at least 10 characters");
        }

        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Pharmacy not found with ID: " + pharmacyId));

        if (pharmacy.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Only PENDING pharmacies can be rejected");
        }

        pharmacy.setApprovalStatus(ApprovalStatus.REJECTED);
        pharmacy.setRejectionReason(reason.trim());
        pharmacy = pharmacyRepository.save(pharmacy);

        eventPublisher.publishEvent(new PharmacyRejectedEvent(
                pharmacy.getId(),
                pharmacy.getUser().getId(),
                pharmacy.getName(),
                reason.trim()
        ));

        notificationPublisher.publish(
                pharmacy.getUser().getId(),
                NotificationType.PHARMACY_REJECTED,
                "Pharmacy Application Rejected",
                "Your pharmacy registration was rejected: " + reason.trim(),
                null
        );

        return toSummaryDto(pharmacy);
    }

    @Transactional
    public PharmacySummaryDto suspendPharmacy(Long pharmacyId, String reason) {
        if (reason == null || reason.trim().length() < 10) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "reason", "Suspension reason must be at least 10 characters");
        }

        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Pharmacy not found with ID: " + pharmacyId));

        if (pharmacy.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Only APPROVED pharmacies can be suspended");
        }

        pharmacy.setApprovalStatus(ApprovalStatus.SUSPENDED);
        pharmacy.setRejectionReason(reason.trim());
        pharmacy = pharmacyRepository.save(pharmacy);

        notificationPublisher.publish(
                pharmacy.getUser().getId(),
                NotificationType.PHARMACY_SUSPENDED,
                "Pharmacy Suspended",
                "Your pharmacy has been suspended: " + reason.trim(),
                null
        );

        return toSummaryDto(pharmacy);
    }

    @Transactional(readOnly = true)
    public PharmacySummaryDto getOwnPharmacy() {
        Long userId = currentUserProvider.userId();
        Pharmacy pharmacy = pharmacyRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No pharmacy registered for current user"));

        return toSummaryDto(pharmacy);
    }

    @Transactional
    public PharmacySummaryDto updateOwnPharmacy(UpdatePharmacyRequest req) {
        Long userId = currentUserProvider.userId();
        Pharmacy pharmacy = pharmacyRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No pharmacy registered for current user"));

        pharmacy.setName(req.name().trim());
        if (req.gstNo() != null) pharmacy.setGstNo(req.gstNo().trim());
        pharmacy.setAddress(req.address().trim());
        if (req.city() != null) pharmacy.setCity(req.city().trim());
        if (req.pincode() != null) pharmacy.setPincode(req.pincode().trim());
        if (req.latitude() != null) pharmacy.setLatitude(req.latitude());
        if (req.longitude() != null) pharmacy.setLongitude(req.longitude());

        // licence number remains strictly locked and untouched
        pharmacy = pharmacyRepository.save(pharmacy);
        return toSummaryDto(pharmacy);
    }

    private PharmacySummaryDto toSummaryDto(Pharmacy p) {
        return new PharmacySummaryDto(
                p.getId(),
                p.getUser().getId(),
                p.getName(),
                p.getLicenseNo(),
                p.getGstNo(),
                p.getAddress(),
                p.getCity(),
                p.getPincode(),
                p.getLatitude(),
                p.getLongitude(),
                p.getApprovalStatus(),
                p.isActive(),
                null,
                MapLinks.directionsUrl(p.getLatitude(), p.getLongitude())
        );
    }
}
