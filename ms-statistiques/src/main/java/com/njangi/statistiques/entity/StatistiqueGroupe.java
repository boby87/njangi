package com.njangi.statistiques.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "statistique_groupe",
    schema = "statistiques",
    indexes = {
        @Index(name = "idx_stat_groupe_id",  columnList = "groupe_id"),
        @Index(name = "idx_stat_session_id", columnList = "session_id"),
        @Index(name = "idx_stat_calcule_le", columnList = "calcule_le")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_stat_groupe_session",
            columnNames = {"groupe_id", "session_id"}
        )
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatistiqueGroupe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    /** Somme totale des paiements validés sur la session */
    @Column(name = "total_collecte", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalCollecte = BigDecimal.ZERO;

    /** Nombre de membres actifs sur la session */
    @Column(name = "nb_membres", nullable = false)
    @Builder.Default
    private int nbMembres = 0;

    /**
     * Taux de participation aux paiements (0.00 – 100.00).
     * Calculé comme : (nbPaiementsValides / nbPaiementsAttendus) × 100
     */
    @Column(name = "taux_participation", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxParticipation = BigDecimal.ZERO;

    /**
     * Taux de présence aux réunions (0.00 – 100.00).
     * Calculé comme : (nbPresences / nbReunions) × 100
     */
    @Column(name = "taux_presence", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxPresence = BigDecimal.ZERO;

    /** Date et heure du dernier calcul */
    @Column(name = "calcule_le", nullable = false)
    private LocalDateTime calculeLe;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        calculeLe = LocalDateTime.now();
    }
}
