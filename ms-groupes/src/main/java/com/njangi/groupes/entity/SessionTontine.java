package com.njangi.groupes.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "session_tontine", schema = "groupes")
public class SessionTontine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "libelle", nullable = false, length = 200)
    private String libelle;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "montant_cagnotte_par_seance", precision = 15, scale = 2)
    private BigDecimal montantCagnotteParSeance;

    @Column(name = "nombre_tours_total")
    private Integer nombreToursTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    private StatutSession statut = StatutSession.PLANIFIEE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SessionTontine() {
    }

    public SessionTontine(UUID id, UUID groupeId, String libelle, LocalDate dateDebut,
                          LocalDate dateFin, BigDecimal montantCagnotteParSeance,
                          Integer nombreToursTotal, StatutSession statut) {
        this.id = id;
        this.groupeId = groupeId;
        this.libelle = libelle;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.montantCagnotteParSeance = montantCagnotteParSeance;
        this.nombreToursTotal = nombreToursTotal;
        this.statut = statut != null ? statut : StatutSession.PLANIFIEE;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = StatutSession.PLANIFIEE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public BigDecimal getMontantCagnotteParSeance() {
        return montantCagnotteParSeance;
    }

    public void setMontantCagnotteParSeance(BigDecimal montantCagnotteParSeance) {
        this.montantCagnotteParSeance = montantCagnotteParSeance;
    }

    public Integer getNombreToursTotal() {
        return nombreToursTotal;
    }

    public void setNombreToursTotal(Integer nombreToursTotal) {
        this.nombreToursTotal = nombreToursTotal;
    }

    public StatutSession getStatut() {
        return statut;
    }

    public void setStatut(StatutSession statut) {
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
}
