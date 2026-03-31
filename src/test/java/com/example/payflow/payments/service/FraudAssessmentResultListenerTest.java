package com.example.payflow.payments.service;

import com.example.payflow.shared.events.FraudAssessmentCompleted;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FraudAssessmentResultListenerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private FraudAssessmentResultListener listener;

    @Test
    void on_delegatesToPaymentService() {
        var correlationId = UUID.randomUUID().toString();
        var transactionId = UUID.randomUUID();
        var event = new FraudAssessmentCompleted(correlationId, transactionId, 100, "APPROVE");

        listener.on(event);

        verify(paymentService).processFraudAssessment(transactionId, "APPROVE", correlationId);
    }
}
