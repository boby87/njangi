package com.njangi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requête d'authentification sociale (Google / Facebook)")
public record SocialLoginRequest(
        @Schema(description = "Fournisseur d'identité ('GOOGLE' ou 'FACEBOOK')", example = "GOOGLE")
        @NotBlank(message = "Le fournisseur est obligatoire (GOOGLE ou FACEBOOK)")
        String provider,

        @Schema(description = "Jeton d'authentification social (Google ID token ou Facebook Access token)", example = "ya29.a0AfH6SM...")
        String token,

        @Schema(description = "Identifiant unique chez le fournisseur (Google 'sub' ou Facebook 'id')", example = "109876543210987654321")
        @NotBlank(message = "L'identifiant du fournisseur (providerId) est obligatoire")
        String providerId,

        @Schema(description = "Adresse email fournie par le réseau social", example = "mbarga.social@gmail.com")
        String email,

        @Schema(description = "Nom de famille", example = "Mbarga")
        String nom,

        @Schema(description = "Prénom", example = "Paul")
        String prenom,

        @Schema(description = "Photo de profil sociale", example = "https://lh3.googleusercontent.com/a/...")
        String photoUrl
) {}
