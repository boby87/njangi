package com.njangi.finances.penalite.dto;

import com.njangi.finances.penalite.entity.TarificationPenalite;
import com.njangi.finances.penalite.entity.TypeInfraction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TarificationPenaliteDto(
        UUID id,
        UUID groupeId,
        TypeInfraction typeInfraction,
        BigDecimal montant,
        boolean actif,
        LocalDateTime createdAt
) {
    public static TarificationPenaliteDto from(TarificationPenalite t) {
        return new TarificationPenaliteDto(
                t.getId(),
                t.getGroupeId(),
                t.getTypeInfraction(),
                t.getMontant(),
                t.isActif(),
                t.getCreatedAt()
        );
    }
}
