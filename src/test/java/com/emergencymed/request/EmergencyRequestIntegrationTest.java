package com.emergencymed.request;

import com.emergencymed.common.contract.InventoryService;
import com.emergencymed.common.contract.NotificationPublisher;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.common.dto.PharmacyDto;
import com.emergencymed.request.entity.EmergencyRequest;
import com.emergencymed.request.entity.RequestItem;
import com.emergencymed.request.exception.InvalidStateTransitionException;
import com.emergencymed.request.repository.EmergencyRequestRepository;
import com.emergencymed.request.repository.RequestStatusHistoryRepository;
import com.emergencymed.request.service.RequestStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmergencyRequestIntegrationTest {

    private EmergencyRequestRepository requestRepository;
    private RequestStatusHistoryRepository historyRepository;
    private InventoryService inventoryService;
    private NotificationPublisher notificationPublisher;
    private PharmacyLookup pharmacyLookup;
    private RequestStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        requestRepository = mock(EmergencyRequestRepository.class);
        historyRepository = mock(RequestStatusHistoryRepository.class);
        inventoryService = mock(InventoryService.class);
        notificationPublisher = mock(NotificationPublisher.class);
        pharmacyLookup = mock(PharmacyLookup.class);

        stateMachine = new RequestStateMachine(
                requestRepository,
                historyRepository,
                inventoryService,
                notificationPublisher,
                pharmacyLookup,
                120
        );

        when(requestRepository.save(any(EmergencyRequest.class))).thenAnswer(i -> i.getArgument(0));
        PharmacyDto p = new PharmacyDto();
        p.setUserId(3L);
        when(pharmacyLookup.findApproved(anyLong())).thenReturn(Optional.of(p));
    }

    @Test
    @DisplayName("Accepting PENDING self-pickup request moves status to READY_FOR_PICKUP")
    void testAcceptSelfPickupRequest() {
        EmergencyRequest req = new EmergencyRequest();
        req.setId(1L);
        req.setReferenceCode("REQ-PICKUP-01");
        req.setStatus("PENDING");
        req.setFulfilmentType("SELF_PICKUP");
        req.setPriority("URGENT");
        req.setRequesterUserId(10L);
        req.setPharmacyId(5L);
        req.setItems(List.of());

        EmergencyRequest result = stateMachine.transitionToAccepted(req, 3L);

        assertEquals("READY_FOR_PICKUP", result.getStatus());
        assertNotNull(result.getAcceptedAt());
        assertNotNull(result.getExpiresAt());
        verify(notificationPublisher, times(1)).publish(eq(10L), eq("REQUEST_ACCEPTED"), anyString(), anyString(), eq(1L));
    }

    @Test
    @DisplayName("Accepting PENDING delivery request moves status to FINDING_DELIVERY")
    void testAcceptDeliveryRequest() {
        EmergencyRequest req = new EmergencyRequest();
        req.setId(2L);
        req.setReferenceCode("REQ-DELIVERY-01");
        req.setStatus("PENDING");
        req.setFulfilmentType("DELIVERY");
        req.setPriority("CRITICAL");
        req.setRequesterUserId(10L);
        req.setPharmacyId(5L);
        req.setItems(List.of());

        EmergencyRequest result = stateMachine.transitionToAccepted(req, 3L);

        assertEquals("FINDING_DELIVERY", result.getStatus());
        assertNotNull(result.getAcceptedAt());
        verify(notificationPublisher, times(1)).publish(eq(10L), eq("REQUEST_ACCEPTED"), anyString(), anyString(), eq(2L));
    }

    @Test
    @DisplayName("Illegal transition throws InvalidStateTransitionException")
    void testIllegalStateTransitionThrows() {
        EmergencyRequest req = new EmergencyRequest();
        req.setId(3L);
        req.setReferenceCode("REQ-DELIVERED-01");
        req.setStatus("DELIVERED"); // Already delivered

        assertThrows(InvalidStateTransitionException.class, () -> {
            stateMachine.transitionToAccepted(req, 3L);
        });
    }

    @Test
    @DisplayName("Unverified prescription blocks pharmacy acceptance")
    void testUnverifiedPrescriptionBlocksAcceptance() {
        EmergencyRequest req = new EmergencyRequest();
        req.setId(4L);
        req.setStatus("PENDING");
        req.setFulfilmentType("DELIVERY");
        req.setPrescriptionStatus("PENDING"); // Unverified prescription

        RequestItem rxItem = new RequestItem();
        rxItem.setRequiresPrescription(true);
        req.setItems(List.of(rxItem));

        assertThrows(InvalidStateTransitionException.class, () -> {
            stateMachine.transitionToAccepted(req, 3L);
        });
    }

    @Test
    @DisplayName("Rejecting request releases reserved inventory stock")
    void testRejectRequestReleasesStock() {
        EmergencyRequest req = new EmergencyRequest();
        req.setId(5L);
        req.setReferenceCode("REQ-REJECT-01");
        req.setStatus("PENDING");
        req.setPharmacyId(5L);
        req.setRequesterUserId(10L);

        RequestItem item = new RequestItem();
        item.setMedicineId(101L);
        item.setInventoryId(101L);
        item.setQuantity(2);
        req.setItems(List.of(item));

        stateMachine.transitionToRejected(req, 3L, "Out of stock on shelf");

        assertEquals("REJECTED", req.getStatus());
        verify(inventoryService, times(1)).release(eq(101L), eq(2), eq("REQ-REJECT-01"));
    }
}
