package com.njangi.identite.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requête d'authentification sociale SSO (Google OIDC)")
public record SocialLoginRequest(
        @Schema(description = "Fournisseur d'identité ('GOOGLE')", example = "GOOGLE")
        @NotBlank(message = "Le fournisseur est obligatoire (GOOGLE)")
        String provider,

        @Schema(description = "Jeton d'identité OIDC signé (Google ID Token / credential JWT)", example = "eyJhbGciOiJSUzI1NiIs...")
        String token,

        @Schema(description = "Identifiant unique chez le fournisseur (optionnel en OIDC réel — extrait du JWT vérifié)", example = "109876543210987654321")
        String providerId,

        @Schema(description = "Adresse email (optionnelle en OIDC réel — extraite du JWT vérifié)", example = "mbarga.social@gmail.com")
        String email,

        @Schema(description = "Nom de famille (optionnel en OIDC réel — extrait du JWT vérifié)", example = "Mbarga")
        String nom,

        @Schema(description = "Prénom (optionnel en OIDC réel — extrait du JWT vérifié)", example = "Paul")
        String prenom,

        @Schema(description = "Photo de profil sociale (optionnelle en OIDC réel — extraite du JWT vérifié)", example = "https://lh3.googleusercontent.com/a/...")
        String photoUrl
) {}
