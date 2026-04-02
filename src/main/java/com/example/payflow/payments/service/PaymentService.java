package com.example.payflow.payments.service;

import com.example.payflow.fraud.domain.FraudDecision;
import com.example.payflow.payments.PaymentNotFoundException;
import com.example.payflow.payments.api.request.SubmitPaymentRequest;
import com.example.payflow.payments.api.response.PaymentResponse;
import com.example.payflow.payments.domain.Payment;
import com.example.payflow.payments.domain.PaymentMethod;
import com.example.payflow.payments.infra.PaymentRepository;
import com.example.payflow.shared.events.DomainEventPublisher;
import com.example.payflow.shared.events.PaymentTransactionAuthorized;
import com.example.payflow.shared.events.PaymentTransactionDeclined;
import com.example.payflow.shared.events.PaymentTransactionInitiated;
import com.example.payflow.shared.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DomainEventPublisher eventPublisher;

    public PaymentResponse submit(UUID merchantId, String merchantEmail, SubmitPaymentRequest request) {
        var existing = paymentRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        var paymentMethod = PaymentMethod.of(request.paymentMethod().type(), request.paymentMethod().token());
        var payment = Payment.create(
                UUID.randomUUID(),
                request.idempotencyKey(),
                merchantId,
                merchantEmail,
                request.payeeAccountId(),
                request.amount(),
                request.currency(),
                paymentMethod
        );

        paymentRepository.save(payment);

        eventPublisher.publish(new PaymentTransactionInitiated(
                payment.getId(),
                payment.getMerchantId(),
                payment.getMerchantEmail(),
                payment.getCorrelationId().toString(),
                payment.getAmount(),
                payment.getCurrency()
        ));

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByMerchant(UUID merchantId, PaymentStatus status) {
        var payments = status != null
                ? paymentRepository.findByMerchantIdAndStatus(merchantId, status)
                : paymentRepository.findByMerchantId(merchantId);
        return payments.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse findById(UUID merchantId, UUID id) {
        return paymentRepository.findByIdAndMerchantId(id, merchantId)
                .map(this::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    @Transactional
    public void processFraudAssessment(UUID paymentId, String decision, String correlationId) {
        var payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        var fraudDecision = FraudDecision.valueOf(decision.trim().toUpperCase(Locale.ROOT));
        
        if (fraudDecision == FraudDecision.APPROVE) {
            payment.authorise();
            paymentRepository.save(payment);
            eventPublisher.publish(new PaymentTransactionAuthorized(
                    correlationId,
                    payment.getId(),
                    payment.getMerchantId(),
                    payment.getMerchantEmail(),
                    payment.getPayeeAccountId(),
                    payment.getAmount(),
                    payment.getCurrency()));
        } else {
            payment.decline();
            paymentRepository.save(payment);
            eventPublisher.publish(new PaymentTransactionDeclined(
                    correlationId, payment.getId(), payment.getMerchantId(), payment.getMerchantEmail(), "Failed fraud assessment"));
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getCorrelationId(),
                payment.getMerchantId(),
                payment.getPayeeAccountId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getCreatedAt()
        );
    }
}
