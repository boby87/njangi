package com.njangi.membres.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record InscrireMembreCommand(
        UUID authUtilisateurId,
        String nom,
        String prenom,
        String email,
        String telephone,
        LocalDate dateNaissance,
        String adresse,
        String ville
) {}
