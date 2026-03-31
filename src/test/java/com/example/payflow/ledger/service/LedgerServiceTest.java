package com.example.payflow.ledger.service;

import com.example.payflow.ledger.domain.JournalEntry;
import com.example.payflow.ledger.infra.JournalEntryRepository;
import com.example.payflow.shared.events.DomainEventPublisher;
import com.example.payflow.shared.events.LedgerEntryPosted;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private LedgerService ledgerService;

    @Test
    void postEntry_persistsJournalEntryAndPublishesEvent() {
        var correlationId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        var merchantId = UUID.randomUUID();
        var payeeAccountId = UUID.randomUUID();
        when(journalEntryRepository.existsByPaymentId(paymentId)).thenReturn(false);
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ledgerService.postEntry(correlationId, paymentId, merchantId, payeeAccountId, new BigDecimal("150.00"), "USD");

        var entryCaptor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository).save(entryCaptor.capture());
        var entry = entryCaptor.getValue();

        assertThat(entry.getCorrelationId()).isEqualTo(correlationId);
        assertThat(entry.getPaymentId()).isEqualTo(paymentId);
        assertThat(entry.getDebitAccount()).isEqualTo("merchant:" + merchantId);
        assertThat(entry.getCreditAccount()).isEqualTo("payee:" + payeeAccountId);
        assertThat(entry.getAmount()).isEqualByComparingTo("150.00");
        assertThat(entry.getCurrency()).isEqualTo("USD");

        var eventCaptor = ArgumentCaptor.forClass(LedgerEntryPosted.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        var event = eventCaptor.getValue();

        assertThat(event.getCorrelationId()).isEqualTo(correlationId.toString());
        assertThat(event.getJournalEntryId()).isEqualTo(entry.getId());
        assertThat(event.getPaymentId()).isEqualTo(paymentId);
    }

    @Test
    void postEntry_isIdempotentForExistingPayment() {
        var paymentId = UUID.randomUUID();
        when(journalEntryRepository.existsByPaymentId(paymentId)).thenReturn(true);

        ledgerService.postEntry(UUID.randomUUID(), paymentId, UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("150.00"), "USD");

        verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void findByCorrelationId_returnsMappedResponses() {
        var correlationId = UUID.randomUUID();
        var entry = JournalEntry.post(correlationId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("150.00"), "USD");
        when(journalEntryRepository.findByCorrelationIdOrderByPostedAtAsc(correlationId)).thenReturn(List.of(entry));

        var responses = ledgerService.findByCorrelationId(correlationId);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(entry.getId());
        assertThat(responses.getFirst().correlationId()).isEqualTo(correlationId);
    }
}
