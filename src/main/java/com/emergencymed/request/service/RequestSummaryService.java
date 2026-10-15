package com.emergencymed.request.service;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.enums.Role;
import com.emergencymed.request.dto.RequestSummaryResponse;
import com.emergencymed.request.repository.EmergencyRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestSummaryService {

    private final EmergencyRequestRepository requestRepository;
    private final CurrentUserProvider currentUserProvider;

    public RequestSummaryService(EmergencyRequestRepository requestRepository, CurrentUserProvider currentUserProvider) {
        this.requestRepository = requestRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public RequestSummaryResponse getSummary() {
        Role role = currentUserProvider.role();
        Long userId = currentUserProvider.userId();

        long pending = 0;
        long inProgress = 0;
        long completed = 0;
        long cancelled = 0;

        if (role == Role.ADMIN) {
            pending = requestRepository.countByStatus("PENDING");
            inProgress = requestRepository.countByStatus("ACCEPTED")
                    + requestRepository.countByStatus("FINDING_DELIVERY")
                    + requestRepository.countByStatus("DELIVERY_ASSIGNED")
                    + requestRepository.countByStatus("READY_FOR_PICKUP")
                    + requestRepository.countByStatus("OUT_FOR_DELIVERY");
            completed = requestRepository.countByStatus("DELIVERED")
                    + requestRepository.countByStatus("PICKED_UP");
            cancelled = requestRepository.countByStatus("CANCELLED")
                    + requestRepository.countByStatus("REJECTED")
                    + requestRepository.countByStatus("EXPIRED");
        } else if (role == Role.PHARMACY) {
            Long pharmacyId = currentUserProvider.pharmacyId().orElse(1L);
            pending = requestRepository.countByPharmacyIdAndStatus(pharmacyId, "PENDING");
            inProgress = requestRepository.countByPharmacyIdAndStatus(pharmacyId, "ACCEPTED")
                    + requestRepository.countByPharmacyIdAndStatus(pharmacyId, "FINDING_DELIVERY")
                    + requestRepository.countByPharmacyIdAndStatus(pharmacyId, "DELIVERY_ASSIGNED")
                    + requestRepository.countByPharmacyIdAndStatus(pharmacyId, "READY_FOR_PICKUP")
                    + requestRepository.countByPharmacyIdAndStatus(pharmacyId, "OUT_FOR_DELIVERY");
            completed = requestRepository.countByPharmacyIdAndStatus(pharmacyId, "DELIVERED")
                    + requestRepository.countByPharmacyIdAndStatus(pharmacyId, "PICKED_UP");
            cancelled = requestRepository.countByPharmacyIdAndStatus(pharmacyId, "CANCELLED")
                    + requestRepository.countByPharmacyIdAndStatus(pharmacyId, "REJECTED")
                    + requestRepository.countByPharmacyIdAndStatus(pharmacyId, "EXPIRED");
        } else if (role == Role.DELIVERY) {
            inProgress = requestRepository.countByDeliveryPartnerUserIdAndStatus(userId, "DELIVERY_ASSIGNED")
                    + requestRepository.countByDeliveryPartnerUserIdAndStatus(userId, "OUT_FOR_DELIVERY");
            completed = requestRepository.countByDeliveryPartnerUserIdAndStatus(userId, "DELIVERED");
            cancelled = requestRepository.countByDeliveryPartnerUserIdAndStatus(userId, "CANCELLED");
        } else {
            // PATIENT / HOSPITAL
            pending = requestRepository.countByRequesterUserIdAndStatus(userId, "PENDING");
            inProgress = requestRepository.countByRequesterUserIdAndStatus(userId, "ACCEPTED")
                    + requestRepository.countByRequesterUserIdAndStatus(userId, "FINDING_DELIVERY")
                    + requestRepository.countByRequesterUserIdAndStatus(userId, "DELIVERY_ASSIGNED")
                    + requestRepository.countByRequesterUserIdAndStatus(userId, "READY_FOR_PICKUP")
                    + requestRepository.countByRequesterUserIdAndStatus(userId, "OUT_FOR_DELIVERY");
            completed = requestRepository.countByRequesterUserIdAndStatus(userId, "DELIVERED")
                    + requestRepository.countByRequesterUserIdAndStatus(userId, "PICKED_UP");
            cancelled = requestRepository.countByRequesterUserIdAndStatus(userId, "CANCELLED")
                    + requestRepository.countByRequesterUserIdAndStatus(userId, "REJECTED")
                    + requestRepository.countByRequesterUserIdAndStatus(userId, "EXPIRED");
        }

        long total = pending + inProgress + completed + cancelled;
        return new RequestSummaryResponse(pending, inProgress, completed, cancelled, total);
    }
}
