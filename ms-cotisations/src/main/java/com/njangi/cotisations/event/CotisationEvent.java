package com.njangi.cotisations.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CotisationEvent(
    String type,
    UUID referenceId,
    UUID groupeId,
    UUID membreId,
    BigDecimal montant,
    Instant timestamp,
    String details
) {
    public static CotisationEvent of(String type, UUID referenceId, UUID groupeId, UUID membreId, BigDecimal montant, String details) {
        return new CotisationEvent(type, referenceId, groupeId, membreId, montant, Instant.now(), details);
    }
}
