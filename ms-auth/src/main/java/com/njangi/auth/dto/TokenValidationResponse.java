package com.njangi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Résultat de la vérification d'un jeton JWT")
public record TokenValidationResponse(
        boolean valide,
        UUID userId,
        String telephone,
        String email,
        String nomComplet,
        String message
) {
    public static TokenValidationResponse valid(UUID userId, String telephone, String email, String nomComplet) {
        return new TokenValidationResponse(true, userId, telephone, email, nomComplet, "Jeton JWT valide");
    }

    public static TokenValidationResponse invalid(String message) {
        return new TokenValidationResponse(false, null, null, null, null, message);
    }
}
