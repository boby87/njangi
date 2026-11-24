package com.njangi.engagement.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DomainEvent(
        UUID eventId,
        String eventType,
        String sourceService,
        UUID groupeId,
        UUID acteurId,
        Instant occurredAt,
        Map<String, Object> payload
) {
    public static final String SOURCE_ENGAGEMENT = "ms-engagement";

    public static DomainEvent of(String eventType, UUID groupeId, UUID acteurId, Map<String, Object> payload) {
        return new DomainEvent(
                UUID.randomUUID(),
                eventType,
                SOURCE_ENGAGEMENT,
                groupeId,
                acteurId,
                Instant.now(),
                payload != null ? payload : Map.of()
        );
    }
}
