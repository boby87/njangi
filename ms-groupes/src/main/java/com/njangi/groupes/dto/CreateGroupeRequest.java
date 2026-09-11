package com.njangi.groupes.dto;

import com.njangi.groupes.entity.TypeSiege;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateGroupeRequest(
    @NotBlank String nom,
    String description,
    @NotNull UUID createurMembreId,
    @NotNull TypeSiege typeSiege,
    String adresseSiege,
    @NotNull @Positive BigDecimal montantCotisationPrincipale,
    @NotBlank String frequenceReunion,
    @Positive Integer nombreMembresMax
) {}
