package com.njangi.penalites.dto;

import com.njangi.penalites.entity.Penalite;
import com.njangi.penalites.entity.Penalite.StatutPenalite;
import com.njangi.penalites.entity.TypeInfraction;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PenaliteDto(
        UUID id,
        @NotNull(message = "L'identifiant du membre est obligatoire") UUID membreId,
        @NotNull(message = "L'identifiant du groupe est obligatoire") UUID groupeId,
        @NotNull(message = "L'identifiant de la session est obligatoire") UUID sessionId,
        @NotNull(message = "Le type d'infraction est obligatoire") TypeInfraction typeInfraction,
        BigDecimal montant,
        StatutPenalite statut,
        LocalDateTime creeLe
) {
    public static PenaliteDto from(Penalite p) {
        return new PenaliteDto(
                p.getId(),
                p.getMembreId(),
                p.getGroupeId(),
                p.getSessionId(),
                p.getTypeInfraction(),
                p.getMontant(),
                p.getStatut(),
                p.getCreeLe()
        );
    }
}
