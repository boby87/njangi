package com.njangi.paiements.dto;

import com.njangi.paiements.entity.ModePaiement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaiementRequest(
        @NotNull UUID cotisationId,
        @NotNull UUID membreId,
        @NotNull UUID groupeId,
        @NotNull @Positive BigDecimal montant,
        @NotNull ModePaiement modePaiement,
        String reference,
        String commentaire
) {}
