package com.njangi.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

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
