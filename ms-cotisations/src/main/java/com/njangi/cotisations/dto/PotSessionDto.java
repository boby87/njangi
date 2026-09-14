package com.njangi.cotisations.dto;

import com.njangi.cotisations.entity.ModeVersement;
import com.njangi.cotisations.entity.StatutPot;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PotSessionDto(
    UUID id,
    UUID groupeId,
    UUID sessionTontineId,
    UUID reunionId,
    UUID membreBeneficiaireId,
    int ordrePassage,
    BigDecimal montantTotal,
    BigDecimal montantNet,
    StatutPot statut,
    ModeVersement modeVersement,
    String referencePaiement,
    LocalDateTime dateAttribution,
    LocalDateTime dateVersement,
    LocalDateTime createdAt
) {
}
