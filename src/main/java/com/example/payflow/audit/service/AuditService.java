package com.example.payflow.audit.service;

import com.example.payflow.audit.domain.AuditEventLog;
import com.example.payflow.audit.infra.AuditEventLogRepository;
import com.example.payflow.shared.events.DomainEvent;
import com.example.payflow.shared.events.FraudAssessmentCompleted;
import com.example.payflow.shared.events.LedgerEntryPosted;
import com.example.payflow.shared.events.PaymentTransactionAuthorized;
import com.example.payflow.shared.events.PaymentTransactionDeclined;
import com.example.payflow.shared.events.PaymentTransactionInitiated;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditService {

    private final AuditEventLogRepository auditEventLogRepository;
    private final ObjectMapper objectMapper;

    public void record(DomainEvent event) {
        var payload = serialize(event);
        var eventLog = AuditEventLog.record(
                event,
                resolveMerchantId(event),
                resolvePaymentId(event),
                resolveEventType(event),
                payload
        );
        auditEventLogRepository.save(eventLog);
    }

    @Transactional(readOnly = true)
    public java.util.List<AuditEventResponse> findByMerchantId(java.util.UUID merchantId) {
        return auditEventLogRepository.findByMerchantIdOrderByOccurredAtDesc(merchantId)
                .stream()
                .map(event -> new AuditEventResponse(
                        event.getId(),
                        event.getCorrelationId(),
                        event.getEventType(),
                        event.getPayload(),
                        event.getOccurredAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.List<AuditEventResponse> findByMerchantIdAndPaymentId(java.util.UUID merchantId, java.util.UUID paymentId) {
        return auditEventLogRepository.findByMerchantIdAndPaymentIdOrderByOccurredAtAsc(merchantId, paymentId)
                .stream()
                .map(event -> new AuditEventResponse(
                        event.getId(),
                        event.getCorrelationId(),
                        event.getEventType(),
                        event.getPayload(),
                        event.getOccurredAt()
                ))
                .toList();
    }

    private java.util.UUID resolveMerchantId(DomainEvent event) {
        return switch (event) {
            case PaymentTransactionInitiated initiated -> initiated.getMerchantId();
            case FraudAssessmentCompleted completed -> completed.getMerchantId();
            case PaymentTransactionAuthorized authorized -> authorized.getMerchantId();
            case PaymentTransactionDeclined declined -> declined.getMerchantId();
            case LedgerEntryPosted posted -> posted.getMerchantId();
            default -> throw new IllegalArgumentException("Unsupported event type for merchant extraction: " + event.getClass().getName());
        };
    }

    private java.util.UUID resolvePaymentId(DomainEvent event) {
        return switch (event) {
            case PaymentTransactionInitiated initiated -> initiated.getPaymentId();
            case FraudAssessmentCompleted completed -> completed.getTransactionId();
            case PaymentTransactionAuthorized authorized -> authorized.getPaymentId();
            case PaymentTransactionDeclined declined -> declined.getPaymentId();
            case LedgerEntryPosted posted -> posted.getPaymentId();
            default -> throw new IllegalArgumentException("Unsupported event type for payment extraction: " + event.getClass().getName());
        };
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize domain event for audit logging", e);
        }
    }

    private String resolveEventType(DomainEvent event) {
        return switch (event) {
            case PaymentTransactionInitiated ignored -> "Payment.Transaction.Initiated";
            case FraudAssessmentCompleted ignored -> "Fraud.Assessment.Completed";
            case PaymentTransactionAuthorized ignored -> "Payment.Transaction.Authorised";
            case PaymentTransactionDeclined ignored -> "Payment.Transaction.Declined";
            case LedgerEntryPosted ignored -> "Ledger.Entry.Posted";
            default -> event.getClass().getSimpleName();
        };
    }
}
