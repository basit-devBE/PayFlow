package com.example.payflow.audit.service;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
        UUID eventId,
        UUID correlationId,
        String eventType,
        String payload,
        Instant occurredAt
) {}
