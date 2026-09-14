package com.njangi.penalites.dto;

import com.njangi.penalites.entity.Penalite;
import com.njangi.penalites.entity.StatutPenalite;
import com.njangi.penalites.entity.TypeInfraction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PenaliteDto(
    UUID id,
    UUID membreId,
    UUID groupeId,
    UUID sessionId,
    UUID reunionId,
    TypeInfraction typeInfraction,
    BigDecimal montant,
    StatutPenalite statut,
    String motif,
    LocalDateTime datePaiement,
    LocalDateTime creeLe
) {
    public static PenaliteDto from(Penalite p) {
        return new PenaliteDto(
            p.getId(),
            p.getMembreId(),
            p.getGroupeId(),
            p.getSessionId(),
            p.getReunionId(),
            p.getTypeInfraction(),
            p.getMontant(),
            p.getStatut(),
            p.getMotif(),
            p.getDatePaiement(),
            p.getCreeLe()
        );
    }
}
