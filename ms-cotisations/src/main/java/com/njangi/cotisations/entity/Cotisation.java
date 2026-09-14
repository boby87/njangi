package com.njangi.cotisations.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cotisation", schema = "cotisations")
public class Cotisation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "membre_id", nullable = false)
    private UUID membreId;

    @Column(name = "type_cotisation_id", nullable = false)
    private UUID typeCotisationId;

    @Column(name = "reunion_id", nullable = false)
    private UUID reunionId;

    @Column(name = "montant_du", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantDu;

    @Column(name = "montant_paye", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantPaye = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    private StatutCotisation statut = StatutCotisation.EN_ATTENTE;

    @Column(name = "date_limite_paiement")
    private LocalDate dateLimitePaiement;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Cotisation() {
    }

    public Cotisation(UUID id, UUID groupeId, UUID membreId, UUID typeCotisationId,
                      UUID reunionId, BigDecimal montantDu, BigDecimal montantPaye,
                      StatutCotisation statut, LocalDate dateLimitePaiement) {
        this.id = id;
        this.groupeId = groupeId;
        this.membreId = membreId;
        this.typeCotisationId = typeCotisationId;
        this.reunionId = reunionId;
        this.montantDu = montantDu;
        this.montantPaye = montantPaye != null ? montantPaye : BigDecimal.ZERO;
        this.statut = statut != null ? statut : StatutCotisation.EN_ATTENTE;
        this.dateLimitePaiement = dateLimitePaiement;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
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

    public UUID getGroupeId() {
        return groupeId;
    }

    public void setGroupeId(UUID groupeId) {
        this.groupeId = groupeId;
    }

    public UUID getMembreId() {
        return membreId;
    }

    public void setMembreId(UUID membreId) {
        this.membreId = membreId;
    }

    public UUID getTypeCotisationId() {
        return typeCotisationId;
    }

    public void setTypeCotisationId(UUID typeCotisationId) {
        this.typeCotisationId = typeCotisationId;
    }

    public UUID getReunionId() {
        return reunionId;
    }

    public void setReunionId(UUID reunionId) {
        this.reunionId = reunionId;
    }

    public BigDecimal getMontantDu() {
        return montantDu;
    }

    public void setMontantDu(BigDecimal montantDu) {
        this.montantDu = montantDu;
    }

    public BigDecimal getMontantPaye() {
        return montantPaye;
    }

    public void setMontantPaye(BigDecimal montantPaye) {
        this.montantPaye = montantPaye;
    }

    public StatutCotisation getStatut() {
        return statut;
    }

    public void setStatut(StatutCotisation statut) {
        this.statut = statut;
    }

    public LocalDate getDateLimitePaiement() {
        return dateLimitePaiement;
    }

    public void setDateLimitePaiement(LocalDate dateLimitePaiement) {
        this.dateLimitePaiement = dateLimitePaiement;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
        if (!(o instanceof Cotisation that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
