package com.njangi.cotisations.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MettreAJourPaiementCotisationRequest(
    @NotNull(message = "Le montant versé est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant versé doit être supérieur à zéro")
    BigDecimal montantVerse
) {
}
