package com.njangi.groupes.dto;

import com.njangi.groupes.entity.TypeSiege;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ModifierGroupeRequest(
        @NotBlank(message = "Le nom du groupe est obligatoire")
        String nom,
        String description,
        @NotNull(message = "Le type de siège est obligatoire")
        TypeSiege typeSiege,
        String adresseSiege,
        @NotNull(message = "Le montant de la cotisation est obligatoire")
        @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
        BigDecimal montantCotisationPrincipale,
        @NotBlank(message = "La fréquence est obligatoire")
        String frequenceReunion,
        Integer nombreMembresMax,
        String reglementInterieur
) {}
