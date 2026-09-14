package com.njangi.paiements.dto;

import jakarta.validation.constraints.NotBlank;

public record WebhookPaiementRequest(
    @NotBlank(message = "La référence de paiement est obligatoire")
    String reference,

    String cleIdempotence,

    boolean succes,

    String referenceExterneOperateur,

    String message
) {
}
