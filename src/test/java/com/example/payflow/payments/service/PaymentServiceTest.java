package com.example.payflow.payments.service;

import com.example.payflow.payments.PaymentNotFoundException;
import com.example.payflow.payments.api.request.PaymentMethodRequest;
import com.example.payflow.payments.api.request.SubmitPaymentRequest;
import com.example.payflow.payments.domain.Payment;
import com.example.payflow.payments.domain.PaymentMethodType;
import com.example.payflow.payments.infra.PaymentRepository;
import com.example.payflow.shared.PaymentStatus;
import com.example.payflow.shared.events.DomainEventPublisher;
import com.example.payflow.shared.events.PaymentTransactionAuthorized;
import com.example.payflow.shared.events.PaymentTransactionDeclined;
import com.example.payflow.shared.events.PaymentTransactionInitiated;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void submit_returnsExistingPayment_whenIdempotencyKeyAlreadyExists() {
        var existingPayment = payment();
        when(paymentRepository.findByIdempotencyKey("idem-123")).thenReturn(Optional.of(existingPayment));

        var response = paymentService.submit(existingPayment.getMerchantId(), submitPaymentRequest());

        assertThat(response.id()).isEqualTo(existingPayment.getId());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void submit_persistsPaymentAndPublishesInitiatedEvent() {
        when(paymentRepository.findByIdempotencyKey("idem-123")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var merchantId = UUID.randomUUID();
        var request = submitPaymentRequest();

        var response = paymentService.submit(merchantId, request);

        var paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        var savedPayment = paymentCaptor.getValue();

        assertThat(savedPayment.getMerchantId()).isEqualTo(merchantId);
        assertThat(savedPayment.getAmount()).isEqualByComparingTo("150.00");
        assertThat(savedPayment.getCurrency()).isEqualTo("USD");
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        var eventCaptor = ArgumentCaptor.forClass(PaymentTransactionInitiated.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        var event = eventCaptor.getValue();

        assertThat(event.getPaymentId()).isEqualTo(savedPayment.getId());
        assertThat(event.getMerchantId()).isEqualTo(merchantId);
        assertThat(event.getAmount()).isEqualByComparingTo("150.00");
        assertThat(event.getCurrency()).isEqualTo("USD");
        assertThat(response.id()).isEqualTo(savedPayment.getId());
        assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void processFraudAssessment_authorisesPaymentAndPublishesAuthorizedEvent() {
        var payment = payment();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        paymentService.processFraudAssessment(payment.getId(), "APPROVE", payment.getCorrelationId().toString());

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORISED);
        verify(paymentRepository).save(payment);

        var eventCaptor = ArgumentCaptor.forClass(PaymentTransactionAuthorized.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        var event = eventCaptor.getValue();

        assertThat(event.getCorrelationId()).isEqualTo(payment.getCorrelationId().toString());
        assertThat(event.getPaymentId()).isEqualTo(payment.getId());
        assertThat(event.getMerchantId()).isEqualTo(payment.getMerchantId());
        assertThat(event.getPayeeAccountId()).isEqualTo(payment.getPayeeAccountId());
        assertThat(event.getAmount()).isEqualByComparingTo(payment.getAmount());
        assertThat(event.getCurrency()).isEqualTo(payment.getCurrency());
    }

    @Test
    void processFraudAssessment_declinesPaymentAndPublishesDeclinedEvent() {
        var payment = payment();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        paymentService.processFraudAssessment(payment.getId(), "DECLINE", payment.getCorrelationId().toString());

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DECLINED);
        verify(paymentRepository).save(payment);

        var eventCaptor = ArgumentCaptor.forClass(PaymentTransactionDeclined.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        var event = eventCaptor.getValue();

        assertThat(event.getCorrelationId()).isEqualTo(payment.getCorrelationId().toString());
        assertThat(event.getPaymentId()).isEqualTo(payment.getId());
        assertThat(event.getMerchantId()).isEqualTo(payment.getMerchantId());
        assertThat(event.getReason()).isEqualTo("Failed fraud assessment");
    }

    @Test
    void processFraudAssessment_throwsWhenPaymentDoesNotExist() {
        var paymentId = UUID.randomUUID();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.processFraudAssessment(paymentId, "APPROVE", UUID.randomUUID().toString()))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void findByMerchant_appliesOptionalStatusFilter() {
        var payment = payment();
        when(paymentRepository.findByMerchantId(payment.getMerchantId())).thenReturn(List.of(payment));

        var responses = paymentService.findByMerchant(payment.getMerchantId(), null);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(payment.getId());
        verify(paymentRepository).findByMerchantId(payment.getMerchantId());
    }

    private static SubmitPaymentRequest submitPaymentRequest() {
        return new SubmitPaymentRequest(
                UUID.randomUUID(),
                "idem-123",
                new BigDecimal("150.00"),
                "USD",
                new PaymentMethodRequest(PaymentMethodType.CARD, "tok_test_1234")
        );
    }

    private static Payment payment() {
        return Payment.create(
                UUID.randomUUID(),
                "idem-123",
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("150.00"),
                "USD",
                com.example.payflow.payments.domain.PaymentMethod.of(PaymentMethodType.CARD, "tok_test_1234")
        );
    }
}
