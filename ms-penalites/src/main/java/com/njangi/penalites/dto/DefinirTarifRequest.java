package com.njangi.penalites.dto;

import com.njangi.penalites.entity.TypeInfraction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record DefinirTarifRequest(
    @NotNull(message = "L'ID du groupe est obligatoire")
    UUID groupeId,

    @NotNull(message = "Le type d'infraction est obligatoire")
    TypeInfraction typeInfraction,

    @NotNull(message = "Le montant de l'amende est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant de l'amende doit être supérieur à zéro")
    BigDecimal montant,

    boolean actif
) {
}
