package com.emergencymed.payment.controller;

import com.emergencymed.common.dto.ApiResponse;
import com.emergencymed.payment.dto.*;
import com.emergencymed.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Retrieve checkout summary, itemized costs, delivery fee, and eligible payment methods.
     */
    @GetMapping("/orders/{orderId}/checkout-summary")
    public ApiResponse<OrderCheckoutSummaryDto> getCheckoutSummary(@PathVariable Long orderId) {
        OrderCheckoutSummaryDto summary = paymentService.getCheckoutSummary(orderId);
        return ApiResponse.ok("Checkout summary retrieved successfully", summary);
    }

    /**
     * Initiate payment for an emergency order (idempotent with idempotencyKey).
     */
    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentResponse> initiatePayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.initiatePayment(request);
        return ApiResponse.created("Payment initiated successfully", response);
    }

    /**
     * Authoritative online payment verification.
     */
    @PostMapping("/payments/verify")
    public ApiResponse<PaymentResponse> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        PaymentResponse response = paymentService.verifyPayment(request);
        return ApiResponse.ok("Payment verification completed", response);
    }

    /**
     * Gateway-independent webhook / callback receiver.
     */
    @PostMapping("/payments/callback")
    public ApiResponse<PaymentResponse> handleCallback(@RequestBody PaymentCallbackRequest callback) {
        PaymentResponse response = paymentService.handleCallback(callback);
        return ApiResponse.ok("Callback processed successfully", response);
    }

    /**
     * Confirm cash collection for COD payments (authorized delivery partner, pharmacy, or admin).
     */
    @PostMapping("/payments/{paymentId}/confirm-cash")
    public ApiResponse<PaymentResponse> confirmCash(
            @PathVariable Long paymentId,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        PaymentResponse response = paymentService.confirmCashCollection(paymentId, notes);
        return ApiResponse.ok("Cash collection confirmed successfully", response);
    }

    /**
     * Retry an eligible failed or cancelled payment.
     */
    @PostMapping("/payments/{paymentId}/retry")
    public ApiResponse<PaymentResponse> retryPayment(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.retryPayment(paymentId);
        return ApiResponse.ok("Payment retry initiated", response);
    }

    /**
     * Get payment details by payment ID.
     */
    @GetMapping("/payments/{paymentId}")
    public ApiResponse<PaymentResponse> getPaymentById(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.getPaymentById(paymentId);
        return ApiResponse.ok(response);
    }

    /**
     * Get payment details for an order.
     */
    @GetMapping("/orders/{orderId}/payment")
    public ApiResponse<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ApiResponse.ok(response);
    }

    /**
     * Get current user's payment history.
     */
    @GetMapping("/payments/my")
    public ApiResponse<List<PaymentResponse>> getMyPayments() {
        List<PaymentResponse> payments = paymentService.getMyPayments();
        return ApiResponse.ok(payments);
    }
}
