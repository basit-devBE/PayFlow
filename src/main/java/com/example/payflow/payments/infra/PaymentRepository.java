package com.example.payflow.payments.infra;

import com.example.payflow.payments.domain.Payment;
import com.example.payflow.shared.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByIdAndMerchantId(UUID id, UUID merchantId);
    List<Payment> findByMerchantIdAndStatus(UUID merchantId, PaymentStatus status);
    List<Payment> findByMerchantId(UUID merchantId);
}
