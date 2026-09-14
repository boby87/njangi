package com.njangi.cotisations.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GenererCotisationsReunionRequest(
    @NotNull(message = "L'ID du groupe est obligatoire")
    UUID groupeId,

    @NotNull(message = "L'ID de la réunion est obligatoire")
    UUID reunionId,

    @NotEmpty(message = "La liste des membres actifs est obligatoire")
    List<UUID> membreIds,

    LocalDate dateLimitePaiement
) {
}
