package com.njangi.groupes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreerSessionRequest(
        @NotBlank(message = "Le libellé de la session est obligatoire")
        String libelle,
        @NotNull(message = "La date de début est obligatoire")
        LocalDate dateDebut,
        LocalDate dateFin,
        @DecimalMin(value = "0.0", inclusive = false, message = "Le montant de la cagnotte par séance doit être positif")
        BigDecimal montantCagnotteParSeance,
        Integer nombreToursTotal
) {}
