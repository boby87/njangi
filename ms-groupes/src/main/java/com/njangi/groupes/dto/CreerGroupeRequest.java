package com.njangi.groupes.dto;

import com.njangi.groupes.entity.TypeSiege;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreerGroupeRequest(
        @NotBlank(message = "Le nom du groupe est obligatoire")
        String nom,
        String description,
        @NotNull(message = "L'identifiant du créateur est obligatoire")
        UUID createurMembreId,
        @NotNull(message = "Le type de siège (FIXE ou ROTATIF) est obligatoire")
        TypeSiege typeSiege,
        String adresseSiege,
        @NotNull(message = "Le montant de la cotisation principale est obligatoire")
        @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être supérieur à 0")
        BigDecimal montantCotisationPrincipale,
        @NotBlank(message = "La fréquence des réunions est obligatoire")
        String frequenceReunion,
        Integer nombreMembresMax,
        String reglementInterieur
) {}
