package com.njangi.penalites.event;

import com.njangi.penalites.entity.TypeInfraction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PenaliteEvent(
    String type,
    UUID penaliteId,
    UUID membreId,
    UUID groupeId,
    TypeInfraction typeInfraction,
    BigDecimal montant,
    String statut,
    Instant timestamp,
    String details
) {
    public static PenaliteEvent of(String type, UUID penaliteId, UUID membreId, UUID groupeId,
                                  TypeInfraction typeInfraction, BigDecimal montant, String statut, String details) {
        return new PenaliteEvent(type, penaliteId, membreId, groupeId, typeInfraction, montant, statut, Instant.now(), details);
    }
}
