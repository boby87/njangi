package com.njangi.reunions.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantReunionDto(
        UUID id,
        UUID reunionId,
        UUID membreId,
        Boolean present,
        Boolean procuration,
        LocalDateTime heureArrivee
) {}
