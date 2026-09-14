package com.njangi.paiements.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record InitierPaiementCashRequest(
    @NotNull(message = "L'ID de la cotisation est obligatoire")
    UUID cotisationId,

    @NotNull(message = "L'ID du membre est obligatoire")
    UUID membreId,

    @NotNull(message = "L'ID du groupe est obligatoire")
    UUID groupeId,

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être supérieur à zéro")
    BigDecimal montant,

    @NotBlank(message = "La clé d'idempotence est obligatoire")
    String cleIdempotence,

    @NotBlank(message = "La preuve de paiement (reçu physique signé) est obligatoire pour les règlements Cash")
    String pieceJointeUrl,

    String commentaire
) {
}
