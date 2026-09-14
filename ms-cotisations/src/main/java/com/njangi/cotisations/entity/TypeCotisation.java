package com.njangi.cotisations.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "type_cotisation", schema = "cotisations")
public class TypeCotisation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "libelle", nullable = false, length = 200)
    private String libelle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false, length = 50)
    private CategorieCotisation categorie = CategorieCotisation.ROTATIVE_POT;

    @Column(name = "montant", nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(name = "est_rotatif", nullable = false)
    private boolean estRotatif = false;

    @Column(name = "est_obligatoire", nullable = false)
    private boolean estObligatoire = true;

    @Column(name = "statut", nullable = false, length = 50)
    private String statut = "ACTIF";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public TypeCotisation() {
    }

    public TypeCotisation(UUID id, UUID groupeId, String libelle, String description,
                          CategorieCotisation categorie, BigDecimal montant,
                          boolean estRotatif, boolean estObligatoire, String statut) {
        this.id = id;
        this.groupeId = groupeId;
        this.libelle = libelle;
        this.description = description;
        this.categorie = categorie != null ? categorie : CategorieCotisation.ROTATIVE_POT;
        this.montant = montant;
        this.estRotatif = estRotatif;
        this.estObligatoire = estObligatoire;
        this.statut = statut != null ? statut : "ACTIF";
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

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CategorieCotisation getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieCotisation categorie) {
        this.categorie = categorie;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public boolean isEstRotatif() {
        return estRotatif;
    }

    public void setEstRotatif(boolean estRotatif) {
        this.estRotatif = estRotatif;
    }

    public boolean isEstObligatoire() {
        return estObligatoire;
    }

    public void setEstObligatoire(boolean estObligatoire) {
        this.estObligatoire = estObligatoire;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
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
        if (!(o instanceof TypeCotisation that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
