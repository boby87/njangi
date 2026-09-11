package com.njangi.membres.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateMembreRequest(
    @NotNull UUID authUtilisateurId,
    @NotBlank String nom,
    @NotBlank String prenom,
    @Email @NotBlank String email,
    @NotBlank String telephone,
    LocalDate dateNaissance,
    String adresse,
    String ville
) {}
