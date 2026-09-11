package com.njangi.reunions.dto;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReunionDto(
    UUID id, UUID groupeId, UUID sessionId,
    LocalDateTime dateHeure, String lieuOuLien,
    UUID membreHoteId, String ordresDuJour, String statut, LocalDateTime creeLe
) {}
