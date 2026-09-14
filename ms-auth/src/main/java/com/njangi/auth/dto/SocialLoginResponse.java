package com.njangi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse d'authentification sociale avec indicateur de complétion du profil")
public record SocialLoginResponse(
        @Schema(description = "Jeton d'accès JWT Njangi")
        String accessToken,

        @Schema(description = "Type de jeton", example = "Bearer")
        String tokenType,

        @Schema(description = "Durée de validité en secondes", example = "86400")
        long expiresIn,

        @Schema(description = "Profil de l'utilisateur connecté")
        UtilisateurDto utilisateur,

        @Schema(description = "Indique si l'utilisateur doit obligatoirement lier un numéro de téléphone pour participer aux tontines", example = "false")
        boolean telephoneRequis
) {
    public static SocialLoginResponse of(String accessToken, long expiresIn, UtilisateurDto utilisateur, boolean telephoneRequis) {
        return new SocialLoginResponse(accessToken, "Bearer", expiresIn, utilisateur, telephoneRequis);
    }
}
