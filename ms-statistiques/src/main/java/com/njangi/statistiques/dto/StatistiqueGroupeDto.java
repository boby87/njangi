package com.njangi.statistiques.dto;

import com.njangi.statistiques.entity.StatistiqueGroupe;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StatistiqueGroupeDto(
        UUID id,
        @NotNull(message = "L'identifiant du groupe est obligatoire") UUID groupeId,
        @NotNull(message = "L'identifiant de la session est obligatoire") UUID sessionId,
        BigDecimal totalCollecte,
        int nbMembres,
        BigDecimal tauxParticipation,
        BigDecimal tauxPresence,
        LocalDateTime calculeLe
) {
    public static StatistiqueGroupeDto from(StatistiqueGroupe s) {
        return new StatistiqueGroupeDto(
                s.getId(),
                s.getGroupeId(),
                s.getSessionId(),
                s.getTotalCollecte(),
                s.getNbMembres(),
                s.getTauxParticipation(),
                s.getTauxPresence(),
                s.getCalculeLe()
        );
    }
}
