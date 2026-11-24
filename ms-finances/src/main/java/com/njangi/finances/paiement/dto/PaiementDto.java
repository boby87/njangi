package com.njangi.finances.paiement.dto;

import com.njangi.finances.paiement.entity.ModePaiement;
import com.njangi.finances.paiement.entity.StatutPaiement;

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
        String cleIdempotence,
        String reference,
        String pieceJointeUrl,
        String numeroTelephone,
        String operateur,
        StatutPaiement statut,
        UUID validePar,
        LocalDateTime dateValidation,
        String commentaire,
        LocalDateTime datePaiement,
        LocalDateTime createdAt
) {
}
