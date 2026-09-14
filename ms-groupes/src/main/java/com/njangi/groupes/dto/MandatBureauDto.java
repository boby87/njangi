package com.njangi.groupes.dto;

import com.njangi.groupes.entity.StatutMandat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MandatBureauDto(
        UUID id,
        UUID groupeId,
        UUID presidentMembreId,
        UUID tresorierMembreId,
        UUID secretaireMembreId,
        UUID vicePresidentMembreId,
        UUID auditeurMembreId,
        LocalDate dateDebut,
        LocalDate dateFin,
        Boolean actif,
        StatutMandat statut,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
