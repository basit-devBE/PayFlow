package com.example.payflow.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "ledger", name = "journal_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JournalEntry {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID correlationId;

    @Column(nullable = false)
    private UUID paymentId;

    @Column(nullable = false, length = 100)
    private String debitAccount;

    @Column(nullable = false, length = 100)
    private String creditAccount;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, updatable = false)
    private Instant postedAt;

    public static JournalEntry post(UUID correlationId, UUID paymentId, UUID merchantId,
                                    UUID payeeAccountId, BigDecimal amount, String currency) {
        var entry = new JournalEntry();
        entry.id = UUID.randomUUID();
        entry.correlationId = correlationId;
        entry.paymentId = paymentId;
        entry.debitAccount = "merchant:" + merchantId;
        entry.creditAccount = "payee:" + payeeAccountId;
        entry.amount = amount;
        entry.currency = currency;
        entry.postedAt = Instant.now();
        return entry;
    }
}
