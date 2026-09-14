package com.njangi.groupes.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ElireBureauRequest(
        @NotNull(message = "L'identifiant du Président élu est obligatoire")
        UUID presidentMembreId,
        @NotNull(message = "L'identifiant du Trésorier élu est obligatoire")
        UUID tresorierMembreId,
        @NotNull(message = "L'identifiant du Secrétaire élu est obligatoire")
        UUID secretaireMembreId,
        UUID vicePresidentMembreId,
        UUID auditeurMembreId,
        @NotNull(message = "La date de début de mandat est obligatoire")
        LocalDate dateDebut,
        LocalDate dateFin
) {}
