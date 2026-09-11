package com.njangi.penalites.dto;

import com.njangi.penalites.entity.TarificationPenalite;
import com.njangi.penalites.entity.TypeInfraction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TarificationPenaliteDto(
        UUID id,
        @NotNull(message = "L'identifiant du groupe est obligatoire") UUID groupeId,
        @NotNull(message = "Le type d'infraction est obligatoire") TypeInfraction typeInfraction,
        @NotNull @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à zéro") BigDecimal montant,
        boolean actif
) {
    public static TarificationPenaliteDto from(TarificationPenalite t) {
        return new TarificationPenaliteDto(
                t.getId(),
                t.getGroupeId(),
                t.getTypeInfraction(),
                t.getMontant(),
                t.isActif()
        );
    }
}
