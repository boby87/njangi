package com.njangi.membres.domain.event;

import com.njangi.membres.domain.model.MembreId;
import java.time.Instant;
import java.util.UUID;

public record MembreInscritEvent(
        MembreId membreId,
        UUID authUtilisateurId,
        String nom,
        String prenom,
        String email,
        String telephone,
        Instant survenuLe
) {
    public MembreInscritEvent(MembreId membreId, UUID authUtilisateurId, String nom, String prenom, String email, String telephone) {
        this(membreId, authUtilisateurId, nom, prenom, email, telephone, Instant.now());
    }
}
