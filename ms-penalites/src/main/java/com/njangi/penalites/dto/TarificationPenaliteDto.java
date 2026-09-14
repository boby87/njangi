package com.njangi.penalites.dto;

import com.njangi.penalites.entity.TarificationPenalite;
import com.njangi.penalites.entity.TypeInfraction;
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
