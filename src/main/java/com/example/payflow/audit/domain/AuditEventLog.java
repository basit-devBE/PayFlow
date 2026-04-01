package com.example.payflow.audit.domain;

import com.example.payflow.shared.events.DomainEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "audit", name = "event_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditEventLog {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID correlationId;

    @Column(nullable = false)
    private UUID merchantId;

    @Column(nullable = false)
    private UUID paymentId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    public static AuditEventLog record(DomainEvent event, UUID merchantId, UUID paymentId, String eventType, String payload) {
        var log = new AuditEventLog();
        log.id = UUID.fromString(event.getEventId());
        log.correlationId = UUID.fromString(event.getCorrelationId());
        log.merchantId = merchantId;
        log.paymentId = paymentId;
        log.eventType = eventType;
        log.payload = payload;
        log.occurredAt = event.getOccurredAt();
        return log;
    }
}
