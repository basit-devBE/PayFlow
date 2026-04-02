package com.example.payflow.ledger.service;

import com.example.payflow.shared.events.PaymentTransactionAuthorized;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthorizedPaymentListenerTest {

    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private AuthorizedPaymentListener listener;

    @Test
    void on_delegatesToLedgerService() {
        var correlationId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        var merchantId = UUID.randomUUID();
        var payeeAccountId = UUID.randomUUID();
        var event = new PaymentTransactionAuthorized(
                correlationId.toString(),
                paymentId,
                merchantId,
                "merchant@example.com",
                payeeAccountId,
                new BigDecimal("150.00"),
                "USD"
        );

        listener.on(event);

        verify(ledgerService).postEntry(
                correlationId,
                paymentId,
                merchantId,
                payeeAccountId,
                new BigDecimal("150.00"),
                "USD"
        );
    }
}
