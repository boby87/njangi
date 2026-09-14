package com.njangi.reunions.dto;

import com.njangi.reunions.entity.TypeSiege;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateReunionRequest(
        @NotBlank(message = "Le titre est obligatoire")
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
