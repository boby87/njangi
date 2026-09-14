package com.njangi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse d'authentification réussie avec jeton JWT")
public record LoginResponse(
        @Schema(description = "Jeton d'accès JWT")
        String accessToken,

        @Schema(description = "Type de jeton", example = "Bearer")
        String tokenType,

        @Schema(description = "Durée de validité en secondes", example = "86400")
        long expiresIn,

        @Schema(description = "Profil de l'utilisateur connecté")
        UtilisateurDto utilisateur
) {
    public static LoginResponse of(String accessToken, long expiresIn, UtilisateurDto utilisateur) {
        return new LoginResponse(accessToken, "Bearer", expiresIn, utilisateur);
    }
}
