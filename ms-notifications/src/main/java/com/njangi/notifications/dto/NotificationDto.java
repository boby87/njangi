package com.njangi.notifications.dto;

import com.njangi.notifications.entity.Notification;
import com.njangi.notifications.entity.Notification.Canal;
import com.njangi.notifications.entity.Notification.StatutNotification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        /** Identifiant du membre destinataire (correspond à destinataire_id en base) */
        @NotNull(message = "L'identifiant du destinataire est obligatoire") UUID destinatreId,
        UUID groupeId,
        @NotNull(message = "Le canal est obligatoire") Canal canal,
        @NotBlank(message = "Le type est obligatoire") String type,
        @NotBlank(message = "Le titre est obligatoire") String titre,
        @NotBlank(message = "Le contenu est obligatoire") String contenu,
        StatutNotification statut,
        boolean lue,
        LocalDateTime creeLe,
        LocalDateTime envoyeeLe
) {
    public static NotificationDto from(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getDestinatreId(),
                n.getGroupeId(),
                n.getCanal(),
                n.getType(),
                n.getTitre(),
                n.getContenu(),
                n.getStatut(),
                n.isLue(),
                n.getCreeLe(),
                n.getEnvoyeeLe()
        );
    }
}
