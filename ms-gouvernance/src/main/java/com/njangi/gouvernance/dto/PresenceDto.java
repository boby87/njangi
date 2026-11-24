package com.njangi.gouvernance.dto;

import com.njangi.gouvernance.entity.StatutPresence;

import java.time.LocalDateTime;
import java.util.UUID;

public record PresenceDto(
        UUID id,
        UUID reunionId,
        UUID membreId,
        StatutPresence statut,
        LocalDateTime heureArrivee,
        String justification,
        Boolean procuration,
        UUID mandataireId,
        LocalDateTime enregistreLe
) {}
