package com.njangi.notifications.dto;

import com.njangi.notifications.entity.Canal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnvoyerNotificationRequest(
        @NotNull(message = "L'identifiant du destinataire est obligatoire")
        UUID destinataireId,

        UUID groupeId,

        @NotNull(message = "Le canal de notification est obligatoire")
        Canal canal,

        @NotBlank(message = "Le type de notification est obligatoire")
        String type,

        @NotBlank(message = "Le titre est obligatoire")
        String titre,

        @NotBlank(message = "Le contenu est obligatoire")
        String contenu,

        String referenceObjet,
        String typeObjet,
        String destinataireContact
) {}
