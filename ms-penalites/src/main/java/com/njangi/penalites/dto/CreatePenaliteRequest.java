package com.njangi.penalites.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePenaliteRequest(
        @NotNull(message = "L'identifiant du groupe est obligatoire")
        UUID groupeId,

        @NotNull(message = "L'identifiant du membre est obligatoire")
        UUID membreId,

        UUID cotisationId,

        @NotBlank(message = "Le motif est obligatoire")
        String motif,

        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être positif")
        BigDecimal montant,

        @NotNull(message = "La date de la pénalité est obligatoire")
        LocalDate datePenalite,

        @NotNull(message = "L'identifiant de l'applicateur est obligatoire")
        UUID appliquePar
) {}
