package com.njangi.cotisations.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record VerserPotRequest(
        @NotNull UUID groupeId,
        @NotNull UUID sessionTontineId,
        @NotNull UUID reunionId,
        @NotNull UUID membreBeneficiaireId,
        @NotNull @Positive BigDecimal montantTotal
) {}
