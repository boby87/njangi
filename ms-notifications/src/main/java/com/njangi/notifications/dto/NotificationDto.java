package com.njangi.notifications.dto;

import com.njangi.notifications.entity.Canal;
import com.njangi.notifications.entity.Notification;
import com.njangi.notifications.entity.StatutNotification;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        UUID destinataireId,
        UUID groupeId,
        Canal canal,
        String type,
        String titre,
        String contenu,
        StatutNotification statut,
        boolean lue,
        LocalDateTime creeLe,
        LocalDateTime envoyeeLe,
        String referenceObjet,
        String typeObjet,
        String destinataireContact
) {
    public static NotificationDto from(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getDestinataireId(),
                n.getGroupeId(),
                n.getCanal(),
                n.getType(),
                n.getTitre(),
                n.getContenu(),
                n.getStatut(),
                n.isLue(),
                n.getCreeLe(),
                n.getEnvoyeeLe(),
                n.getReferenceObjet(),
                n.getTypeObjet(),
                n.getDestinataireContact()
        );
    }
}
