package com.njangi.groupes.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mandat_bureau", schema = "groupes")
public class MandatBureau {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "president_membre_id", nullable = false)
    private UUID presidentMembreId;

    @Column(name = "tresorier_membre_id")
    private UUID tresorierMembreId;

    @Column(name = "secretaire_membre_id")
    private UUID secretaireMembreId;

    @Column(name = "vice_president_membre_id")
    private UUID vicePresidentMembreId;

    @Column(name = "auditeur_membre_id")
    private UUID auditeurMembreId;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    private StatutMandat statut = StatutMandat.EN_COURS;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public MandatBureau() {
    }

    public MandatBureau(UUID id, UUID groupeId, UUID presidentMembreId, UUID tresorierMembreId,
                        UUID secretaireMembreId, UUID vicePresidentMembreId, UUID auditeurMembreId,
                        LocalDate dateDebut, LocalDate dateFin, Boolean actif, StatutMandat statut) {
        this.id = id;
        this.groupeId = groupeId;
        this.presidentMembreId = presidentMembreId;
        this.tresorierMembreId = tresorierMembreId;
        this.secretaireMembreId = secretaireMembreId;
        this.vicePresidentMembreId = vicePresidentMembreId;
        this.auditeurMembreId = auditeurMembreId;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.actif = actif != null ? actif : true;
        this.statut = statut != null ? statut : StatutMandat.EN_COURS;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.actif == null) {
            this.actif = true;
        }
        if (this.statut == null) {
            this.statut = StatutMandat.EN_COURS;
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

    public UUID getPresidentMembreId() {
        return presidentMembreId;
    }

    public void setPresidentMembreId(UUID presidentMembreId) {
        this.presidentMembreId = presidentMembreId;
    }

    public UUID getTresorierMembreId() {
        return tresorierMembreId;
    }

    public void setTresorierMembreId(UUID tresorierMembreId) {
        this.tresorierMembreId = tresorierMembreId;
    }

    public UUID getSecretaireMembreId() {
        return secretaireMembreId;
    }

    public void setSecretaireMembreId(UUID secretaireMembreId) {
        this.secretaireMembreId = secretaireMembreId;
    }

    public UUID getVicePresidentMembreId() {
        return vicePresidentMembreId;
    }

    public void setVicePresidentMembreId(UUID vicePresidentMembreId) {
        this.vicePresidentMembreId = vicePresidentMembreId;
    }

    public UUID getAuditeurMembreId() {
        return auditeurMembreId;
    }

    public void setAuditeurMembreId(UUID auditeurMembreId) {
        this.auditeurMembreId = auditeurMembreId;
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

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public StatutMandat getStatut() {
        return statut;
    }

    public void setStatut(StatutMandat statut) {
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
