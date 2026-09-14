package com.njangi.penalites.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "penalite",
    schema = "penalites",
    indexes = {
        @Index(name = "idx_penalite_membre_id", columnList = "membre_id"),
        @Index(name = "idx_penalite_groupe_id", columnList = "groupe_id"),
        @Index(name = "idx_penalite_session_id", columnList = "session_id"),
        @Index(name = "idx_penalite_reunion_id", columnList = "reunion_id"),
        @Index(name = "idx_penalite_statut", columnList = "statut")
    }
)
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

    @Column(name = "reunion_id")
    private UUID reunionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_infraction", nullable = false, length = 50)
    private TypeInfraction typeInfraction;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutPenalite statut = StatutPenalite.EN_ATTENTE;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Penalite() {
    }

    public Penalite(UUID id, UUID membreId, UUID groupeId, UUID sessionId, UUID reunionId,
                    TypeInfraction typeInfraction, BigDecimal montant, StatutPenalite statut, String motif) {
        this.id = id;
        this.membreId = membreId;
        this.groupeId = groupeId;
        this.sessionId = sessionId;
        this.reunionId = reunionId;
        this.typeInfraction = typeInfraction;
        this.montant = montant;
        this.statut = statut != null ? statut : StatutPenalite.EN_ATTENTE;
        this.motif = motif;
    }

    @PrePersist
    protected void onCreate() {
        creeLe = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (statut == null) {
            statut = StatutPenalite.EN_ATTENTE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMembreId() {
        return membreId;
    }

    public void setMembreId(UUID membreId) {
        this.membreId = membreId;
    }

    public UUID getGroupeId() {
        return groupeId;
    }

    public void setGroupeId(UUID groupeId) {
        this.groupeId = groupeId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getReunionId() {
        return reunionId;
    }

    public void setReunionId(UUID reunionId) {
        this.reunionId = reunionId;
    }

    public TypeInfraction getTypeInfraction() {
        return typeInfraction;
    }

    public void setTypeInfraction(TypeInfraction typeInfraction) {
        this.typeInfraction = typeInfraction;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public StatutPenalite getStatut() {
        return statut;
    }

    public void setStatut(StatutPenalite statut) {
        this.statut = statut;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public LocalDateTime getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(LocalDateTime creeLe) {
        this.creeLe = creeLe;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Penalite that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
