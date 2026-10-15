package com.emergencymed.payment.service;

import com.emergencymed.common.contract.CurrentUserProvider;
import com.emergencymed.common.contract.NotificationPublisher;
import com.emergencymed.common.contract.PharmacyLookup;
import com.emergencymed.common.contract.PharmacySettingsLookup;
import com.emergencymed.common.dto.PharmacyDto;
import com.emergencymed.common.dto.PharmacySettingsDto;
import com.emergencymed.common.enums.Role;
import com.emergencymed.common.exception.BusinessException;
import com.emergencymed.common.exception.ErrorCode;
import com.emergencymed.common.exception.ForbiddenException;
import com.emergencymed.common.exception.ResourceNotFoundException;
import com.emergencymed.common.exception.ValidationException;
import com.emergencymed.payment.dto.*;
import com.emergencymed.payment.entity.Payment;
import com.emergencymed.payment.enums.PaymentMethod;
import com.emergencymed.payment.enums.PaymentStatus;
import com.emergencymed.payment.provider.GatewayInitiationResult;
import com.emergencymed.payment.provider.GatewayVerificationResult;
import com.emergencymed.payment.provider.PaymentGatewayProvider;
import com.emergencymed.payment.repository.PaymentRepository;
import com.emergencymed.request.entity.EmergencyRequest;
import com.emergencymed.request.entity.RequestItem;
import com.emergencymed.request.repository.EmergencyRequestRepository;
import com.emergencymed.request.repository.RequestItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PaymentRepository paymentRepository;
    private final EmergencyRequestRepository requestRepository;
    private final RequestItemRepository requestItemRepository;
    private final PharmacySettingsLookup settingsLookup;
    private final PharmacyLookup pharmacyLookup;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationPublisher notificationPublisher;
    private final PaymentGatewayProvider gatewayProvider;

    public PaymentService(
            PaymentRepository paymentRepository,
            EmergencyRequestRepository requestRepository,
            RequestItemRepository requestItemRepository,
            PharmacySettingsLookup settingsLookup,
            PharmacyLookup pharmacyLookup,
            CurrentUserProvider currentUserProvider,
            NotificationPublisher notificationPublisher,
            PaymentGatewayProvider gatewayProvider) {
        this.paymentRepository = paymentRepository;
        this.requestRepository = requestRepository;
        this.requestItemRepository = requestItemRepository;
        this.settingsLookup = settingsLookup;
        this.pharmacyLookup = pharmacyLookup;
        this.currentUserProvider = currentUserProvider;
        this.notificationPublisher = notificationPublisher;
        this.gatewayProvider = gatewayProvider;
    }

    /**
     * Retrieve checkout summary and validated amounts for an emergency order.
     */
    @Transactional(readOnly = true)
    public OrderCheckoutSummaryDto getCheckoutSummary(Long orderId) {
        EmergencyRequest order = findAndAuthorizeOrder(orderId);
        List<RequestItem> items = requestItemRepository.findByRequestId(orderId);

        BigDecimal medicineCost = BigDecimal.ZERO;
        List<OrderCheckoutSummaryDto.ItemSummary> itemSummaries = new ArrayList<>();
        for (RequestItem item : items) {
            BigDecimal lineTotal = item.getLineTotal() != null ? item.getLineTotal()
                    : item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            medicineCost = medicineCost.add(lineTotal);
            itemSummaries.add(new OrderCheckoutSummaryDto.ItemSummary(
                    item.getMedicineId(),
                    item.getMedicineName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    lineTotal
            ));
        }

        BigDecimal deliveryFee = BigDecimal.ZERO;
        if ("DELIVERY".equalsIgnoreCase(order.getFulfilmentType())) {
            if (order.getDeliveryFee() != null && order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
                deliveryFee = order.getDeliveryFee();
            } else {
                Optional<PharmacySettingsDto> settingsOpt = settingsLookup.find(order.getPharmacyId());
                if (settingsOpt.isPresent() && settingsOpt.get().getDeliveryFee() != null) {
                    deliveryFee = settingsOpt.get().getDeliveryFee();
                }
            }
        }

        BigDecimal total = medicineCost.add(deliveryFee);

        // Pharmacy details and COD support
        Optional<PharmacyDto> pharmacyOpt = pharmacyLookup.findApproved(order.getPharmacyId());
        String pharmacyName = pharmacyOpt.map(PharmacyDto::getName).orElse("Pharmacy #" + order.getPharmacyId());

        boolean codSupported = isCodSupportedForPharmacy(order.getPharmacyId(), order.getFulfilmentType());

        List<PaymentMethod> supportedMethods = new ArrayList<>(List.of(
                PaymentMethod.UPI,
                PaymentMethod.UPI_QR,
                PaymentMethod.DEBIT_CARD,
                PaymentMethod.CREDIT_CARD
        ));
        if (codSupported) {
            supportedMethods.add(PaymentMethod.COD);
        }

        PaymentResponse existingPayment = paymentRepository.findByOrderId(orderId)
                .map(this::toResponse)
                .orElse(null);

        OrderCheckoutSummaryDto dto = new OrderCheckoutSummaryDto();
        dto.setOrderId(order.getId());
        dto.setReferenceCode(order.getReferenceCode());
        dto.setOrderStatus(order.getStatus());
        dto.setPharmacyId(order.getPharmacyId());
        dto.setPharmacyName(pharmacyName);
        dto.setFulfilmentType(order.getFulfilmentType());
        dto.setMedicineCost(medicineCost);
        dto.setDeliveryFee(deliveryFee);
        dto.setTotalAmount(total);
        dto.setCurrency("INR");
        dto.setCodSupported(codSupported);
        dto.setSupportedPaymentMethods(supportedMethods);
        dto.setExistingPayment(existingPayment);
        dto.setItems(itemSummaries);

        return dto;
    }

    /**
     * Initiate payment for a valid emergency request.
     */
    @Transactional
    public PaymentResponse initiatePayment(CreatePaymentRequest request) {
        EmergencyRequest order = findAndAuthorizeOrder(request.getOrderId());

        // Validate order status
        String status = order.getStatus();
        if ("CANCELLED".equals(status) || "REJECTED".equals(status) || "EXPIRED".equals(status)) {
            throw new ValidationException("Cannot initiate payment for order in " + status + " status");
        }

        // Idempotency check
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Payment> existingByIdempotency = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingByIdempotency.isPresent()) {
                log.info("Idempotent payment initiation hit for key={}", request.getIdempotencyKey());
                return toResponse(existingByIdempotency.get());
            }
        }

        // Check if existing payment is already PAID
        Optional<Payment> existingOpt = paymentRepository.findByOrderId(order.getId());
        if (existingOpt.isPresent()) {
            Payment p = existingOpt.get();
            if (p.getPaymentStatus() == PaymentStatus.PAID) {
                throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Order is already paid with reference " + p.getPaymentReference());
            }
            if (p.getPaymentStatus() == PaymentStatus.PENDING && p.getPaymentMethod() == request.getPaymentMethod()) {
                // Return existing pending payment instead of duplicate
                return toResponse(p);
            }
        }

        // Backend amount recalculation
        BigDecimal medicineCost = BigDecimal.ZERO;
        List<RequestItem> items = requestItemRepository.findByRequestId(order.getId());
        for (RequestItem item : items) {
            BigDecimal lineTotal = item.getLineTotal() != null ? item.getLineTotal()
                    : item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            medicineCost = medicineCost.add(lineTotal);
        }

        BigDecimal deliveryFee = BigDecimal.ZERO;
        if ("DELIVERY".equalsIgnoreCase(order.getFulfilmentType())) {
            if (order.getDeliveryFee() != null && order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
                deliveryFee = order.getDeliveryFee();
            } else {
                Optional<PharmacySettingsDto> settingsOpt = settingsLookup.find(order.getPharmacyId());
                if (settingsOpt.isPresent() && settingsOpt.get().getDeliveryFee() != null) {
                    deliveryFee = settingsOpt.get().getDeliveryFee();
                }
            }
        }
        BigDecimal total = medicineCost.add(deliveryFee);

        // Check COD support if requested
        if (request.getPaymentMethod() == PaymentMethod.COD) {
            boolean codOk = isCodSupportedForPharmacy(order.getPharmacyId(), order.getFulfilmentType());
            if (!codOk) {
                throw new ValidationException("Cash on Delivery is not supported by this pharmacy");
            }
        }

        // Create or update payment record
        Payment payment = existingOpt.orElseGet(Payment::new);
        payment.setOrderId(order.getId());
        payment.setUserId(currentUserProvider.userId());
        payment.setPharmacyId(order.getPharmacyId());
        payment.setAmount(total);
        payment.setCurrency("INR");
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setGatewayProvider(gatewayProvider.getProviderName());
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setFailureReason(null);

        if (payment.getPaymentReference() == null) {
            payment.setPaymentReference(generatePaymentReference());
        }

        // Call provider initiation
        GatewayInitiationResult initiationResult = gatewayProvider.initiatePayment(payment);
        payment.setGatewayTransactionId(initiationResult.gatewayTransactionId());

        Payment saved = paymentRepository.save(payment);
        log.info("Payment initiated: ref={}, amount={} {}, method={}, provider={}",
                saved.getPaymentReference(), saved.getAmount(), saved.getCurrency(),
                saved.getPaymentMethod(), saved.getGatewayProvider());

        PaymentResponse resp = toResponse(saved);
        resp.setQrCodeData(initiationResult.qrCodeData());
        resp.setUpiIntentUrl(initiationResult.upiIntentUrl());
        return resp;
    }

    /**
     * Authoritative verification of an online payment result.
     */
    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        Payment payment = paymentRepository.findByPaymentReference(request.getPaymentReference())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for reference: " + request.getPaymentReference()));

        authorizeAccess(payment);

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("Payment ref={} is already verified and PAID", payment.getPaymentReference());
            return toResponse(payment);
        }

        if (payment.getPaymentMethod() == PaymentMethod.COD) {
            throw new ValidationException("COD payments are confirmed on cash handover, not through online verification");
        }

        GatewayVerificationResult result = gatewayProvider.verifyPayment(
                payment,
                request.getGatewayTransactionId(),
                request.getSignature(),
                request.getSimulatedOutcome()
        );

        if (result.success() && result.paymentStatus() == PaymentStatus.PAID) {
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            payment.setGatewayTransactionId(result.gatewayTransactionId());
            payment.setFailureReason(null);

            // Notify user & pharmacy
            try {
                notificationPublisher.publish(
                        payment.getUserId(),
                        "PAYMENT_SUCCESS",
                        "Payment Successful",
                        "Payment of ₹" + payment.getAmount() + " for Order #" + payment.getOrderId() + " was confirmed.",
                        payment.getOrderId()
                );
            } catch (Exception e) {
                log.warn("Could not publish payment notification: {}", e.getMessage());
            }
        } else if (result.paymentStatus() == PaymentStatus.FAILED) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.failureReason());
            try {
                notificationPublisher.publish(
                        payment.getUserId(),
                        "PAYMENT_FAILED",
                        "Payment Failed",
                        "Payment for Order #" + payment.getOrderId() + " failed: " + result.failureReason(),
                        payment.getOrderId()
                );
            } catch (Exception e) {
                log.warn("Could not publish payment failed notification: {}", e.getMessage());
            }
        } else {
            payment.setPaymentStatus(PaymentStatus.PENDING);
        }

        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    /**
     * Webhook/callback handler for gateway-independent asynchronous notifications.
     */
    @Transactional
    public PaymentResponse handleCallback(PaymentCallbackRequest callback) {
        if (callback.getPaymentReference() == null || callback.getPaymentReference().isBlank()) {
            throw new ValidationException("Missing payment reference in callback");
        }

        Payment payment = paymentRepository.findByPaymentReference(callback.getPaymentReference())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for reference: " + callback.getPaymentReference()));

        // Signature validation
        if (!gatewayProvider.validateWebhookSignature(callback.getPaymentReference(), callback.getSignature())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid webhook signature");
        }

        // Amount & currency check
        if (callback.getAmount() != null && payment.getAmount().compareTo(callback.getAmount()) != 0) {
            log.error("Amount mismatch in callback for ref={}: expected {}, got {}",
                    payment.getPaymentReference(), payment.getAmount(), callback.getAmount());
            throw new ValidationException("Payment amount mismatch in webhook");
        }

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("Callback for already PAID payment ref={}, ignoring duplicate", payment.getPaymentReference());
            return toResponse(payment);
        }

        if ("PAID".equalsIgnoreCase(callback.getStatus())) {
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            if (callback.getGatewayTransactionId() != null) {
                payment.setGatewayTransactionId(callback.getGatewayTransactionId());
            }
            payment.setFailureReason(null);
            log.info("Payment marked PAID via webhook: ref={}", payment.getPaymentReference());
        } else if ("FAILED".equalsIgnoreCase(callback.getStatus())) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Provider reported failure via callback");
        }

        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    /**
     * Confirm cash collection for COD orders upon pickup or delivery handover.
     */
    @Transactional
    public PaymentResponse confirmCashCollection(Long paymentId, String notes) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (payment.getPaymentMethod() != PaymentMethod.COD) {
            throw new ValidationException("Only COD payments can be confirmed via cash collection");
        }

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            return toResponse(payment);
        }

        // Authorize: delivery partner, pharmacy staff, or admin
        Role role = currentUserProvider.role();
        Long currentUserId = currentUserProvider.userId();
        if (role != Role.ADMIN && role != Role.DELIVERY && role != Role.PHARMACY) {
            throw new ForbiddenException("Only delivery partners or pharmacy staff can confirm cash collection");
        }

        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setCashCollectedByUserId(currentUserId);
        payment.setCashCollectedAt(LocalDateTime.now());
        payment.setFailureReason(notes);

        Payment saved = paymentRepository.save(payment);
        log.info("COD cash collection confirmed: paymentId={}, collectedBy={}, amount={}",
                saved.getId(), currentUserId, saved.getAmount());

        try {
            notificationPublisher.publish(
                    payment.getUserId(),
                    "PAYMENT_SUCCESS",
                    "COD Cash Confirmed",
                    "Cash collection of ₹" + payment.getAmount() + " confirmed for Order #" + payment.getOrderId(),
                    payment.getOrderId()
            );
        } catch (Exception e) {
            log.warn("Could not publish COD notification: {}", e.getMessage());
        }

        return toResponse(saved);
    }

    /**
     * Retry an eligible failed or cancelled payment.
     */
    @Transactional
    public PaymentResponse retryPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        authorizeAccess(payment);

        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Payment is already PAID");
        }

        // Reset status to PENDING and reinitiate
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setFailureReason(null);
        GatewayInitiationResult init = gatewayProvider.initiatePayment(payment);
        payment.setGatewayTransactionId(init.gatewayTransactionId());

        Payment saved = paymentRepository.save(payment);
        PaymentResponse resp = toResponse(saved);
        resp.setQrCodeData(init.qrCodeData());
        resp.setUpiIntentUrl(init.upiIntentUrl());
        return resp;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        authorizeAccess(payment);
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        EmergencyRequest order = findAndAuthorizeOrder(orderId);
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for Order #" + orderId));
        authorizeAccess(payment);
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments() {
        Long userId = currentUserProvider.userId();
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EmergencyRequest findAndAuthorizeOrder(Long orderId) {
        EmergencyRequest order = requestRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency order not found with ID: " + orderId));

        if (!currentUserProvider.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User must be logged in to access checkout");
        }

        Role role = currentUserProvider.role();
        Long userId = currentUserProvider.userId();
        if (role == Role.ADMIN) {
            return order;
        }

        if (role == Role.PATIENT || role == Role.HOSPITAL) {
            if (!order.getRequesterUserId().equals(userId)) {
                throw new ForbiddenException("You are not authorized to view or pay for this order");
            }
        } else if (role == Role.PHARMACY) {
            Optional<Long> pharmId = currentUserProvider.pharmacyId();
            if (pharmId.isEmpty() || !pharmId.get().equals(order.getPharmacyId())) {
                throw new ForbiddenException("Pharmacy is not authorized for this order");
            }
        }
        return order;
    }

    private void authorizeAccess(Payment payment) {
        Role role = currentUserProvider.role();
        Long userId = currentUserProvider.userId();
        if (role == Role.ADMIN) {
            return;
        }
        if (payment.getUserId().equals(userId)) {
            return;
        }
        if (role == Role.PHARMACY) {
            Optional<Long> pharmId = currentUserProvider.pharmacyId();
            if (pharmId.isPresent() && pharmId.get().equals(payment.getPharmacyId())) {
                return;
            }
        }
        throw new ForbiddenException("You are not authorized to access this payment record");
    }

    private boolean isCodSupportedForPharmacy(Long pharmacyId, String fulfilmentType) {
        Optional<PharmacySettingsDto> opt = settingsLookup.find(pharmacyId);
        if (opt.isEmpty()) {
            return true; // default enabled
        }
        PharmacySettingsDto settings = opt.get();
        if ("DELIVERY".equalsIgnoreCase(fulfilmentType)) {
            return settings.isDeliveryEnabled();
        }
        return true; // Self-pickup always permits cash on handover
    }

    private String generatePaymentReference() {
        return "PAY-" + System.currentTimeMillis() + "-" + (1000 + RANDOM.nextInt(9000));
    }

    private PaymentResponse toResponse(Payment p) {
        PaymentResponse resp = new PaymentResponse();
        resp.setId(p.getId());
        resp.setPaymentReference(p.getPaymentReference());
        resp.setOrderId(p.getOrderId());
        resp.setUserId(p.getUserId());
        resp.setPharmacyId(p.getPharmacyId());
        resp.setAmount(p.getAmount());
        resp.setCurrency(p.getCurrency());
        resp.setPaymentMethod(p.getPaymentMethod());
        resp.setPaymentStatus(p.getPaymentStatus());
        resp.setGatewayProvider(p.getGatewayProvider());
        resp.setGatewayTransactionId(p.getGatewayTransactionId());
        resp.setFailureReason(p.getFailureReason());
        resp.setPaidAt(p.getPaidAt());
        resp.setCashCollectedAt(p.getCashCollectedAt());
        resp.setCreatedAt(p.getCreatedAt());
        resp.setUpdatedAt(p.getUpdatedAt());
        resp.setDemoMode(gatewayProvider.isDemoMode());
        return resp;
    }
}
