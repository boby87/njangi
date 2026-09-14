package com.njangi.groupes.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record GroupeEvent(
        String eventId,
        String eventType,
        UUID groupeId,
        Instant timestamp,
        Map<String, Object> payload
) {
    public static GroupeEvent of(String eventType, UUID groupeId, Map<String, Object> payload) {
        return new GroupeEvent(
                UUID.randomUUID().toString(),
                eventType,
                groupeId,
                Instant.now(),
                payload != null ? payload : Map.of()
        );
    }
}
