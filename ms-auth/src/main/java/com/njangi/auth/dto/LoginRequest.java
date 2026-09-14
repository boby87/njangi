package com.njangi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requête de connexion par identifiant (téléphone/email) et mot de passe")
public record LoginRequest(
        @Schema(description = "Numéro de téléphone (E.164) ou adresse email", example = "+237699123456")
        @NotBlank(message = "L'identifiant est obligatoire")
        String identifiant,

        @Schema(description = "Mot de passe", example = "Secret123!")
        @NotBlank(message = "Le mot de passe est obligatoire")
        String motDePasse
) {}
