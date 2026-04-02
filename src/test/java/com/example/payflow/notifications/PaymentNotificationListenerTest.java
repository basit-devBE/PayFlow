package com.example.payflow.notifications;

import com.example.payflow.shared.events.PaymentTransactionAuthorized;
import com.example.payflow.shared.events.PaymentTransactionDeclined;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationListenerTest {

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private PaymentNotificationListener listener;

    @Test
    void onAuthorized_usesMerchantEmailFromEvent() {
        var paymentId = UUID.randomUUID();
        var event = new PaymentTransactionAuthorized(
                UUID.randomUUID().toString(),
                paymentId,
                UUID.randomUUID(),
                "merchant@example.com",
                UUID.randomUUID(),
                new BigDecimal("150.00"),
                "USD"
        );

        listener.on(event);

        verify(emailNotificationService).sendPaymentAuthorized(
                "merchant@example.com",
                paymentId,
                new BigDecimal("150.00"),
                "USD"
        );
    }

    @Test
    void onDeclined_skipsNotificationWhenMerchantEmailMissing() {
        var paymentId = UUID.randomUUID();
        var event = new PaymentTransactionDeclined(
                UUID.randomUUID().toString(),
                paymentId,
                UUID.randomUUID(),
                " ",
                "Failed fraud assessment"
        );

        listener.on(event);

        verify(emailNotificationService, never()).sendPaymentDeclined(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
