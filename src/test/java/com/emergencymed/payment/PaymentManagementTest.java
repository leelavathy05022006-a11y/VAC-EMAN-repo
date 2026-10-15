package com.emergencymed.payment;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.contract.NotificationPublisher;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.common.contract.PharmacySettingsLookup;
import com.emergencymed.common.dto.PharmacyDto;
import com.emergencymed.common.dto.PharmacySettingsDto;
import com.emergencymed.common.enums.Role;
import com.emergencymed.common.exception.ForbiddenException;
import com.emergencymed.common.exception.ValidationException;
import com.emergencymed.payment.dto.*;
import com.emergencymed.payment.entity.Payment;
import com.emergencymed.payment.enums.PaymentMethod;
import com.emergencymed.payment.enums.PaymentStatus;
import com.emergencymed.payment.provider.DemoPaymentGatewayProvider;
import com.emergencymed.payment.repository.PaymentRepository;
import com.emergencymed.payment.service.PaymentService;
import com.emergencymed.request.entity.EmergencyRequest;
import com.emergencymed.request.entity.RequestItem;
import com.emergencymed.request.repository.EmergencyRequestRepository;
import com.emergencymed.request.repository.RequestItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentManagementTest {

    private PaymentRepository paymentRepository;
    private EmergencyRequestRepository requestRepository;
    private RequestItemRepository requestItemRepository;
    private PharmacySettingsLookup settingsLookup;
    private PharmacyLookup pharmacyLookup;
    private CurrentUserProvider currentUserProvider;
    private NotificationPublisher notificationPublisher;
    private DemoPaymentGatewayProvider gatewayProvider;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        requestRepository = mock(EmergencyRequestRepository.class);
        requestItemRepository = mock(RequestItemRepository.class);
        settingsLookup = mock(PharmacySettingsLookup.class);
        pharmacyLookup = mock(PharmacyLookup.class);
        currentUserProvider = mock(CurrentUserProvider.class);
        notificationPublisher = mock(NotificationPublisher.class);
        gatewayProvider = new DemoPaymentGatewayProvider();

        paymentService = new PaymentService(
                paymentRepository,
                requestRepository,
                requestItemRepository,
                settingsLookup,
                pharmacyLookup,
                currentUserProvider,
                notificationPublisher,
                gatewayProvider
        );

        when(currentUserProvider.isAuthenticated()).thenReturn(true);
        when(currentUserProvider.userId()).thenReturn(1L);
        when(currentUserProvider.role()).thenReturn(Role.PATIENT);
    }

    @Test
    @DisplayName("Checkout summary recalculates backend item amounts and delivery fees accurately")
    void testCheckoutSummaryRecalculation() {
        EmergencyRequest order = new EmergencyRequest();
        order.setId(10L);
        order.setReferenceCode("REQ-2026-001");
        order.setRequesterUserId(1L);
        order.setPharmacyId(5L);
        order.setFulfilmentType("DELIVERY");
        order.setStatus("ACCEPTED");

        when(requestRepository.findById(10L)).thenReturn(Optional.of(order));

        RequestItem item1 = new RequestItem();
        item1.setMedicineId(101L);
        item1.setMedicineName("Adrenaline 1mg");
        item1.setQuantity(2);
        item1.setUnitPrice(BigDecimal.valueOf(150.00));
        item1.setLineTotal(BigDecimal.valueOf(300.00));

        RequestItem item2 = new RequestItem();
        item2.setMedicineId(102L);
        item2.setMedicineName("Salbutamol Inhaler");
        item2.setQuantity(1);
        item2.setUnitPrice(BigDecimal.valueOf(220.00));
        item2.setLineTotal(BigDecimal.valueOf(220.00));

        when(requestItemRepository.findByRequestId(10L)).thenReturn(List.of(item1, item2));

        PharmacySettingsDto settings = new PharmacySettingsDto(5L, true, BigDecimal.valueOf(10.0), BigDecimal.valueOf(50.00));
        when(settingsLookup.find(5L)).thenReturn(Optional.of(settings));

        PharmacyDto pharmacy = new PharmacyDto();
        pharmacy.setName("Apollo 24x7 Emergency");
        when(pharmacyLookup.findApproved(5L)).thenReturn(Optional.of(pharmacy));

        OrderCheckoutSummaryDto summary = paymentService.getCheckoutSummary(10L);

        assertNotNull(summary);
        assertEquals(BigDecimal.valueOf(520.00), summary.getMedicineCost());
        assertEquals(BigDecimal.valueOf(50.00), summary.getDeliveryFee());
        assertEquals(BigDecimal.valueOf(570.00), summary.getTotalAmount());
        assertTrue(summary.isCodSupported());
        assertTrue(summary.getSupportedPaymentMethods().contains(PaymentMethod.COD));
        assertTrue(summary.getSupportedPaymentMethods().contains(PaymentMethod.UPI_QR));
    }

    @Test
    @DisplayName("Initiate payment creates pending payment with idempotency support and demo mode flag")
    void testInitiatePaymentWithIdempotency() {
        EmergencyRequest order = new EmergencyRequest();
        order.setId(10L);
        order.setReferenceCode("REQ-2026-001");
        order.setRequesterUserId(1L);
        order.setPharmacyId(5L);
        order.setFulfilmentType("DELIVERY");
        order.setStatus("ACCEPTED");

        when(requestRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(paymentRepository.findByIdempotencyKey("KEY-123")).thenReturn(Optional.empty());
        when(requestItemRepository.findByRequestId(10L)).thenReturn(List.of());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(99L);
            return p;
        });

        CreatePaymentRequest req = new CreatePaymentRequest(10L, PaymentMethod.UPI_QR, "KEY-123");
        PaymentResponse resp = paymentService.initiatePayment(req);

        assertNotNull(resp);
        assertEquals(PaymentStatus.PENDING, resp.getPaymentStatus());
        assertEquals(PaymentMethod.UPI_QR, resp.getPaymentMethod());
        assertTrue(resp.isDemoMode());
        assertNotNull(resp.getQrCodeData());
        assertNotNull(resp.getPaymentReference());
    }

    @Test
    @DisplayName("Verify payment updates status to PAID on success and notifies user")
    void testVerifyPaymentSuccess() {
        Payment p = new Payment();
        p.setId(99L);
        p.setPaymentReference("PAY-2026-999");
        p.setOrderId(10L);
        p.setUserId(1L);
        p.setPharmacyId(5L);
        p.setAmount(BigDecimal.valueOf(570.00));
        p.setPaymentMethod(PaymentMethod.UPI);
        p.setPaymentStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByPaymentReference("PAY-2026-999")).thenReturn(Optional.of(p));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        VerifyPaymentRequest req = new VerifyPaymentRequest("PAY-2026-999", "TXN-12345", "SIG-ABC");
        req.setSimulatedOutcome("SUCCESS");

        PaymentResponse resp = paymentService.verifyPayment(req);

        assertEquals(PaymentStatus.PAID, resp.getPaymentStatus());
        assertNotNull(resp.getPaidAt());
        verify(notificationPublisher, times(1)).publish(eq(1L), eq("PAYMENT_SUCCESS"), anyString(), anyString(), eq(10L));
    }

    @Test
    @DisplayName("Verify payment marks FAILED when simulated failure outcome is provided")
    void testVerifyPaymentFailure() {
        Payment p = new Payment();
        p.setId(99L);
        p.setPaymentReference("PAY-2026-888");
        p.setOrderId(10L);
        p.setUserId(1L);
        p.setPharmacyId(5L);
        p.setAmount(BigDecimal.valueOf(570.00));
        p.setPaymentMethod(PaymentMethod.DEBIT_CARD);
        p.setPaymentStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByPaymentReference("PAY-2026-888")).thenReturn(Optional.of(p));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        VerifyPaymentRequest req = new VerifyPaymentRequest("PAY-2026-888", "TXN-FAIL-01", "SIG-ABC");
        req.setSimulatedOutcome("FAIL");

        PaymentResponse resp = paymentService.verifyPayment(req);

        assertEquals(PaymentStatus.FAILED, resp.getPaymentStatus());
        assertNotNull(resp.getFailureReason());
        verify(notificationPublisher, times(1)).publish(eq(1L), eq("PAYMENT_FAILED"), anyString(), anyString(), eq(10L));
    }

    @Test
    @DisplayName("Forbidden exception thrown when unauthorized user attempts to access another user's payment")
    void testUnauthorizedPaymentAccessBlocked() {
        Payment p = new Payment();
        p.setId(99L);
        p.setPaymentReference("PAY-2026-SECRET");
        p.setUserId(999L); // Different user
        p.setPharmacyId(5L);

        when(paymentRepository.findById(99L)).thenReturn(Optional.of(p));

        assertThrows(ForbiddenException.class, () -> {
            paymentService.getPaymentById(99L);
        });
    }

    @Test
    @DisplayName("COD cash confirmation allowed only for delivery partner, pharmacy, or admin")
    void testCodCashConfirmation() {
        Payment p = new Payment();
        p.setId(55L);
        p.setOrderId(10L);
        p.setUserId(1L);
        p.setPharmacyId(5L);
        p.setAmount(BigDecimal.valueOf(350.00));
        p.setPaymentMethod(PaymentMethod.COD);
        p.setPaymentStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(55L)).thenReturn(Optional.of(p));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // Attempt as PATIENT -> should fail
        assertThrows(ForbiddenException.class, () -> {
            paymentService.confirmCashCollection(55L, "Cash collected");
        });

        // Attempt as DELIVERY partner -> should succeed
        when(currentUserProvider.role()).thenReturn(Role.DELIVERY);
        when(currentUserProvider.userId()).thenReturn(4L);

        PaymentResponse resp = paymentService.confirmCashCollection(55L, "Cash collected by runner");
        assertEquals(PaymentStatus.PAID, resp.getPaymentStatus());
        assertNotNull(resp.getCashCollectedAt());
    }
}
