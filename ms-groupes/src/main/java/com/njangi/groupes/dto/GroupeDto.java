package com.njangi.groupes.dto;

import com.njangi.groupes.entity.StatutGroupe;
import com.njangi.groupes.entity.TypeSiege;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GroupeDto(
    UUID id,
    String nom,
    String description,
    UUID createurMembreId,
    TypeSiege typeSiege,
    String adresseSiege,
    BigDecimal montantCotisationPrincipale,
    String frequenceReunion,
    Integer nombreMembresMax,
    StatutGroupe statut,
    LocalDateTime createdAt
) {}
