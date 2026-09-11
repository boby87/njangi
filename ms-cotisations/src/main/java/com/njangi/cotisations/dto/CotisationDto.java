package com.njangi.cotisations.dto;

import com.njangi.cotisations.entity.StatutCotisation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CotisationDto(
        UUID id,
        UUID groupeId,
        UUID membreId,
        UUID typeCotisationId,
        UUID reunionId,
        BigDecimal montantDu,
        BigDecimal montantPaye,
        StatutCotisation statut,
        LocalDate dateLimitePaiement,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
