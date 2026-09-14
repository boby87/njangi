package com.njangi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Représentation détaillée d'un utilisateur Njangi")
public record UtilisateurDto(
        UUID id,
        String telephone,
        String email,
        String nom,
        String prenom,
        String photoUrl,
        String ville,
        String pays,
        String statut,
        LocalDateTime creeLe
) {}
