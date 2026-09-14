package com.njangi.paiements.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ValiderPaiementCashRequest(
    @NotNull(message = "L'ID du trésorier validateur est obligatoire")
    UUID tresorierId,

    boolean valide,

    String commentaire
) {
}
