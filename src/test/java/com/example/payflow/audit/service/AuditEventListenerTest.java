package com.example.payflow.audit.service;

import com.example.payflow.shared.events.PaymentTransactionInitiated;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventListenerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditEventListener auditEventListener;

    @Test
    void on_paymentTransactionInitiated_delegatesToAuditService() {
        var event = new PaymentTransactionInitiated(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                new BigDecimal("150.00"),
                "USD"
        );

        auditEventListener.on(event);

        verify(auditService).record(event);
    }
}
