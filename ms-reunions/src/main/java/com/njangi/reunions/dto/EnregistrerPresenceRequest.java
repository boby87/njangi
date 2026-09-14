package com.njangi.reunions.dto;

import com.njangi.reunions.entity.StatutPresence;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record EnregistrerPresenceRequest(
        @NotNull(message = "L'identifiant du membre est obligatoire")
        UUID membreId,
        @NotNull(message = "Le statut de présence est obligatoire")
        StatutPresence statut,
        LocalDateTime heureArrivee,
        String justification,
        Boolean procuration,
        UUID mandataireId
) {}
