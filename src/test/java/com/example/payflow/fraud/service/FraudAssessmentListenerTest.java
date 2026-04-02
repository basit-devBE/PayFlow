package com.example.payflow.fraud.service;

import com.example.payflow.fraud.domain.FraudAssessment;
import com.example.payflow.fraud.domain.FraudDecision;
import com.example.payflow.fraud.infra.FraudAssessmentRepository;
import com.example.payflow.shared.events.DomainEventPublisher;
import com.example.payflow.shared.events.FraudAssessmentCompleted;
import com.example.payflow.shared.events.PaymentTransactionInitiated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudAssessmentListenerTest {

    @Mock
    private FraudAssessmentRepository repository;

    @Mock
    private DomainEventPublisher publisher;

    private FraudAssessmentListener listener;

    @BeforeEach
    void setUp() {
        listener = new FraudAssessmentListener(new FraudRuleEngine(), repository, publisher);
    }

    @Test
    void on_persistsApproveDecisionAndPublishesCompletedEvent() {
        when(repository.save(any(FraudAssessment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var event = initiatedEvent(new BigDecimal("150.00"));

        listener.on(event);

        var assessmentCaptor = ArgumentCaptor.forClass(FraudAssessment.class);
        verify(repository).save(assessmentCaptor.capture());
        var assessment = assessmentCaptor.getValue();

        assertThat(assessment.getTransactionId()).isEqualTo(event.getPaymentId());
        assertThat(assessment.getMerchantId()).isEqualTo(event.getMerchantId());
        assertThat(assessment.getDecision()).isEqualTo(FraudDecision.APPROVE);
        assertThat(assessment.getScore()).isEqualTo(100);

        var completedCaptor = ArgumentCaptor.forClass(FraudAssessmentCompleted.class);
        verify(publisher).publish(completedCaptor.capture());
        var completed = completedCaptor.getValue();

        assertThat(completed.getCorrelationId()).isEqualTo(event.getCorrelationId());
        assertThat(completed.getTransactionId()).isEqualTo(event.getPaymentId());
        assertThat(completed.getMerchantId()).isEqualTo(event.getMerchantId());
        assertThat(completed.getDecision()).isEqualTo("APPROVE");
        assertThat(completed.getScore()).isEqualTo(100);
    }

    @Test
    void on_persistsDeclineDecisionAndPublishesCompletedEvent() {
        when(repository.save(any(FraudAssessment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var event = initiatedEvent(new BigDecimal("10000.01"));

        listener.on(event);

        var completedCaptor = ArgumentCaptor.forClass(FraudAssessmentCompleted.class);
        verify(publisher).publish(completedCaptor.capture());
        var completed = completedCaptor.getValue();

        assertThat(completed.getDecision()).isEqualTo("DECLINE");
        assertThat(completed.getScore()).isEqualTo(0);
    }

    private static PaymentTransactionInitiated initiatedEvent(BigDecimal amount) {
        return new PaymentTransactionInitiated(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "merchant@example.com",
                UUID.randomUUID().toString(),
                amount,
                "USD"
        );
    }
}
