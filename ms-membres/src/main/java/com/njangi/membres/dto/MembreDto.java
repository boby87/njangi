package com.njangi.membres.dto;

import com.njangi.membres.entity.StatutMembre;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MembreDto(
    UUID id,
    UUID authUtilisateurId,
    String nom,
    String prenom,
    String email,
    String telephone,
    LocalDate dateNaissance,
    String adresse,
    String ville,
    String photoUrl,
    StatutMembre statut,
    LocalDateTime createdAt
) {}
