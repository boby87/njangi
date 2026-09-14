package com.njangi.penalites.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "tarification_penalite",
    schema = "penalites",
    indexes = {
        @Index(name = "idx_tarif_groupe_id", columnList = "groupe_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_tarif_groupe_type",
            columnNames = {"groupe_id", "type_infraction"}
        )
    }
)
public class TarificationPenalite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_infraction", nullable = false, length = 50)
    private TypeInfraction typeInfraction;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public TarificationPenalite() {
    }

    public TarificationPenalite(UUID id, UUID groupeId, TypeInfraction typeInfraction, BigDecimal montant, boolean actif) {
        this.id = id;
        this.groupeId = groupeId;
        this.typeInfraction = typeInfraction;
        this.montant = montant;
        this.actif = actif;
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

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
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
        if (!(o instanceof TarificationPenalite that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
