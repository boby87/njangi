package com.njangi.penalites.entity;

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
    name = "penalite",
    schema = "penalites",
    indexes = {
        @Index(name = "idx_penalite_membre_id", columnList = "membre_id"),
        @Index(name = "idx_penalite_groupe_id", columnList = "groupe_id"),
        @Index(name = "idx_penalite_session_id", columnList = "session_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Penalite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "membre_id", nullable = false)
    private UUID membreId;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_infraction", nullable = false, length = 30)
    private TypeInfraction typeInfraction;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutPenalite statut = StatutPenalite.EN_ATTENTE;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @PrePersist
    protected void onCreate() {
        creeLe = LocalDateTime.now();
        if (statut == null) {
            statut = StatutPenalite.EN_ATTENTE;
        }
    }

    public enum StatutPenalite {
        EN_ATTENTE, PAYEE, ANNULEE
    }
}
