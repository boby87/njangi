package com.njangi.reunions.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ReunionEvent(
        String eventId,
        String eventType,
        UUID reunionId,
        UUID groupeId,
        Instant timestamp,
        Map<String, Object> payload
) {
    public static ReunionEvent of(String eventType, UUID reunionId, UUID groupeId, Map<String, Object> payload) {
        return new ReunionEvent(
                UUID.randomUUID().toString(),
                eventType,
                reunionId,
                groupeId,
                Instant.now(),
                payload != null ? payload : Map.of()
        );
    }
}
