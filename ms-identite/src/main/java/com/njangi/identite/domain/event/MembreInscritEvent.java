package com.njangi.identite.domain.event;

import com.njangi.identite.domain.model.MembreId;

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
    public MembreInscritEvent(
            MembreId membreId,
            UUID authUtilisateurId,
            String nom,
            String prenom,
            String email,
            String telephone
    ) {
        this(membreId, authUtilisateurId, nom, prenom, email, telephone, Instant.now());
    }
}
