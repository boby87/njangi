package com.njangi.notifications.dto;

import com.njangi.notifications.entity.TypeNotification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnvoyerNotificationRequest(
        @NotNull(message = "L'identifiant du membre est obligatoire")
        UUID membreId,

        @NotNull(message = "Le type de notification est obligatoire")
        TypeNotification type,

        @NotBlank(message = "Le sujet est obligatoire")
        String sujet,

        @NotBlank(message = "Le contenu est obligatoire")
        String contenu,

        @NotBlank(message = "Le destinataire est obligatoire")
        String destinataire,

        String referenceObjet,

        String typeObjet
) {}
