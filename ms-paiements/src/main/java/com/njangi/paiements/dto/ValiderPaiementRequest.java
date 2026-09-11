package com.njangi.paiements.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ValiderPaiementRequest(
        @NotNull UUID validePar,
        @NotBlank String otpCode,
        String commentaire
) {}
