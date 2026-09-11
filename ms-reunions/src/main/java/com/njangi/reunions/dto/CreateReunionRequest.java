package com.njangi.reunions.dto;

import com.njangi.reunions.entity.TypeSiege;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateReunionRequest(
        @NotNull UUID groupeId,
        @NotBlank String titre,
        @NotNull LocalDateTime dateReunion,
        String lieuReunion,
        @NotNull TypeSiege typeSiege,
        String ordreJour,
        UUID presidentReunionId,
        UUID tresorierReunionId,
        UUID sessionTontineId
) {}
