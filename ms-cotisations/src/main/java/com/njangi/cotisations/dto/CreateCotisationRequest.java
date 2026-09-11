package com.njangi.cotisations.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateCotisationRequest(
        @NotNull UUID groupeId,
        @NotNull UUID membreId,
        @NotNull UUID typeCotisationId,
        @NotNull @Positive BigDecimal montantDu,
        LocalDate dateLimitePaiement,
        UUID reunionId
) {}
