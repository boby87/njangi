package com.njangi.paiements.dto;

import com.njangi.paiements.entity.ModePaiement;
import com.njangi.paiements.entity.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaiementDto(
        UUID id,
        UUID cotisationId,
        UUID membreId,
        UUID groupeId,
        BigDecimal montant,
        ModePaiement modePaiement,
        String reference,
        StatutPaiement statut,
        LocalDateTime datePaiement,
        UUID validePar,
        LocalDateTime dateValidation,
        String commentaire,
        Boolean otpVerifie,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
