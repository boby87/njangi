package com.njangi.penalites.dto;

import com.njangi.penalites.entity.TypeInfraction;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record InfligerPenaliteRequest(
    @NotNull(message = "L'ID du membre est obligatoire")
    UUID membreId,

    @NotNull(message = "L'ID du groupe est obligatoire")
    UUID groupeId,

    @NotNull(message = "L'ID de la session est obligatoire")
    UUID sessionId,

    UUID reunionId,

    @NotNull(message = "Le type d'infraction est obligatoire")
    TypeInfraction typeInfraction,

    BigDecimal montant,

    String motif
) {
}
