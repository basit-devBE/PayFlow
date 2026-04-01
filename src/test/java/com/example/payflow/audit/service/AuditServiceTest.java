package com.example.payflow.audit.service;

import com.example.payflow.audit.infra.AuditEventLogRepository;
import com.example.payflow.shared.events.PaymentTransactionAuthorized;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventLogRepository auditEventLogRepository;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditEventLogRepository, new ObjectMapper());
    }

    @Test
    void record_persistsAuditEventLog() {
        var correlationId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        var merchantId = UUID.randomUUID();
        var payeeAccountId = UUID.randomUUID();
        var event = new PaymentTransactionAuthorized(
                correlationId.toString(),
                paymentId,
                merchantId,
                payeeAccountId,
                new BigDecimal("150.00"),
                "USD"
        );

        auditService.record(event);

        var eventCaptor = ArgumentCaptor.forClass(com.example.payflow.audit.domain.AuditEventLog.class);
        verify(auditEventLogRepository).save(eventCaptor.capture());
        var saved = eventCaptor.getValue();

        assertThat(saved.getId()).isEqualTo(UUID.fromString(event.getEventId()));
        assertThat(saved.getCorrelationId()).isEqualTo(correlationId);
        assertThat(saved.getMerchantId()).isEqualTo(merchantId);
        assertThat(saved.getPaymentId()).isEqualTo(paymentId);
        assertThat(saved.getEventType()).isEqualTo("Payment.Transaction.Authorised");
        assertThat(saved.getPayload()).contains(paymentId.toString());
        assertThat(saved.getPayload()).contains("\"currency\":\"USD\"");
        assertThat(saved.getOccurredAt()).isEqualTo(event.getOccurredAt());
    }
}
