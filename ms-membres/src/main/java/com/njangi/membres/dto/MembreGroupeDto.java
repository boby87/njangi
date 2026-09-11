package com.njangi.membres.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MembreGroupeDto(
    UUID id,
    UUID utilisateurId,
    UUID groupeId,
    String role,
    String statut,
    LocalDateTime rejointLe
) {}
