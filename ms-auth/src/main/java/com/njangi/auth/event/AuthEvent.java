package com.njangi.auth.event;

import java.time.Instant;
import java.util.UUID;

public record AuthEvent(
        String type,
        UUID utilisateurId,
        String identifiant,
        String codeOtp,
        String nomComplet,
        Instant timestamp
) {
    public static AuthEvent otpDemande(String identifiant, String codeOtp) {
        return new AuthEvent("OTP_DEMANDE", null, identifiant, codeOtp, null, Instant.now());
    }

    public static AuthEvent utilisateurInscrit(UUID utilisateurId, String identifiant, String nomComplet) {
        return new AuthEvent("UTILISATEUR_INSCRIT", utilisateurId, identifiant, null, nomComplet, Instant.now());
    }

    public static AuthEvent utilisateurConnecte(UUID utilisateurId, String identifiant) {
        return new AuthEvent("UTILISATEUR_CONNECTE", utilisateurId, identifiant, null, null, Instant.now());
    }
}
