package com.example.payflow.ledger.infra;

import com.example.payflow.ledger.domain.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    List<JournalEntry> findByCorrelationIdAndDebitAccountOrderByPostedAtAsc(UUID correlationId, String debitAccount);
    boolean existsByPaymentId(UUID paymentId);
}
