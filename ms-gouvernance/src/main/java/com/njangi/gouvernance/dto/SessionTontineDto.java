package com.njangi.gouvernance.dto;

import com.njangi.gouvernance.entity.StatutSession;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SessionTontineDto(
        UUID id,
        UUID groupeId,
        String libelle,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal montantCagnotteParSeance,
        Integer nombreToursTotal,
        StatutSession statut,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
