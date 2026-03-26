package com.example.payflow.shared.events;

import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent {

    private final String eventId = UUID.randomUUID().toString();
    private final String correlationId;
    private final Instant occurredAt = Instant.now();

    protected DomainEvent(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
