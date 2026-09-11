package com.njangi.notifications.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "notification",
    schema = "notifications",
    indexes = {
        @Index(name = "idx_notif_dest_id",    columnList = "destinataire_id"),
        @Index(name = "idx_notif_statut",     columnList = "statut"),
        @Index(name = "idx_notif_groupe_id",  columnList = "groupe_id"),
        @Index(name = "idx_notif_lue",        columnList = "lue")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Identifiant du membre destinataire.
     * Nom Java simplifié (destinatreId) pour éviter la confusion orthographique
     * dans les query methods Spring Data. La colonne SQL reste "destinataire_id".
     */
    @Column(name = "destinataire_id", nullable = false)
    private UUID destinatreId;

    @Column(name = "groupe_id")
    private UUID groupeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Canal canal;

    /** Code métier identifiant l'origine de la notification (ex. PAIEMENT_VALIDE, REUNION_RAPPEL) */
    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private StatutNotification statut = StatutNotification.EN_ATTENTE;

    @Column(nullable = false)
    @Builder.Default
    private boolean lue = false;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @Column(name = "envoyee_le")
    private LocalDateTime envoyeeLe;

    @PrePersist
    protected void onCreate() {
        creeLe = LocalDateTime.now();
        if (statut == null) {
            statut = StatutNotification.EN_ATTENTE;
        }
    }

    public enum Canal {
        PUSH, SMS, EMAIL, IN_APP
    }

    public enum StatutNotification {
        EN_ATTENTE, ENVOYEE, ECHEC
    }
}
