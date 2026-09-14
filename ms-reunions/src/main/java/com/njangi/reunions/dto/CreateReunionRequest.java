package com.njangi.reunions.dto;

import com.njangi.reunions.entity.TypeSiege;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateReunionRequest(
        @NotNull(message = "L'identifiant du groupe est obligatoire")
        UUID groupeId,
        UUID sessionTontineId,
        @NotBlank(message = "Le titre de la réunion est obligatoire")
        String titre,
        @NotNull(message = "La date de la réunion est obligatoire")
        LocalDateTime dateReunion,
        String lieuReunion,
        TypeSiege typeSiege,
        UUID hoteId,
        String ordreJour,
        UUID presidentReunionId,
        UUID secretaireReunionId,
        UUID tresorierReunionId
) {}
