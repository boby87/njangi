package com.njangi.gouvernance.dto;

import com.njangi.gouvernance.entity.StatutReunion;
import com.njangi.gouvernance.entity.TypeSiege;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReunionDto(
        UUID id,
        UUID groupeId,
        UUID sessionTontineId,
        String titre,
        LocalDateTime dateReunion,
        String lieuReunion,
        TypeSiege typeSiege,
        UUID hoteId,
        StatutReunion statut,
        String ordreJour,
        String compteRendu,
        UUID presidentReunionId,
        UUID secretaireReunionId,
        UUID tresorierReunionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
