package com.example.payflow.ledger.service;

import com.example.payflow.ledger.api.response.JournalEntryResponse;
import com.example.payflow.ledger.domain.JournalEntry;
import com.example.payflow.ledger.infra.JournalEntryRepository;
import com.example.payflow.shared.events.DomainEventPublisher;
import com.example.payflow.shared.events.LedgerEntryPosted;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LedgerService {

    private final JournalEntryRepository journalEntryRepository;
    private final DomainEventPublisher eventPublisher;

    public void postEntry(UUID correlationId, UUID paymentId, UUID merchantId, UUID payeeAccountId,
                          BigDecimal amount, String currency) {
        if (journalEntryRepository.existsByPaymentId(paymentId)) {
            return;
        }

        var journalEntry = JournalEntry.post(correlationId, paymentId, merchantId, payeeAccountId, amount, currency);
        journalEntryRepository.save(journalEntry);

        eventPublisher.publish(new LedgerEntryPosted(
                correlationId.toString(),
                journalEntry.getId(),
                paymentId,
                amount,
                currency
        ));
    }

    @Transactional(readOnly = true)
    public List<JournalEntryResponse> findByCorrelationId(UUID correlationId) {
        return journalEntryRepository.findByCorrelationIdOrderByPostedAtAsc(correlationId)
                .stream()
                .map(entry -> new JournalEntryResponse(
                        entry.getId(),
                        entry.getCorrelationId(),
                        entry.getPaymentId(),
                        entry.getDebitAccount(),
                        entry.getCreditAccount(),
                        entry.getAmount(),
                        entry.getCurrency(),
                        entry.getPostedAt()
                ))
                .toList();
    }
}
