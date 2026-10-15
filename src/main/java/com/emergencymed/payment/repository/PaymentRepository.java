package com.emergencymed.payment.repository;

import com.emergencymed.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Payment> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);
}
