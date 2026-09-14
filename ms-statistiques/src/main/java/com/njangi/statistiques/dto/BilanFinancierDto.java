package com.njangi.statistiques.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record BilanFinancierDto(
        UUID groupeId,
        UUID sessionId,
        BigDecimal soldeGlobalXAF,
        BigDecimal totalCotisationsXAF,
        BigDecimal totalDecaissementsXAF,
        BigDecimal totalPenalitesXAF,
        Map<String, BigDecimal> repartitionTresorerie,
        BigDecimal tauxRecouvrement,
        BigDecimal tauxAssiduite,
        String statutSanteFinanciere,
        int nbMembres,
        int nbReunions,
        LocalDateTime genereLe
) {}
