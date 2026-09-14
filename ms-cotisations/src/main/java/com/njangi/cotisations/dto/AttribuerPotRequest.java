package com.njangi.cotisations.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record AttribuerPotRequest(
    @NotNull(message = "L'ID de la réunion est obligatoire")
    UUID reunionId,

    BigDecimal montantAjuste
) {
}
