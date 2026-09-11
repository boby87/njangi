package com.njangi.cotisations.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pot_session", schema = "cotisations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PotSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "session_tontine_id", nullable = false)
    private UUID sessionTontineId;

    @Column(name = "reunion_id", nullable = false)
    private UUID reunionId;

    @Column(name = "membre_beneficiaire_id", nullable = false)
    private UUID membreBeneficiaireId;

    @Column(name = "montant_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    @Builder.Default
    private StatutPot statut = StatutPot.EN_ATTENTE;

    @Column(name = "date_versement")
    private LocalDateTime dateVersement;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
