package com.njangi.reunions.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reunion", schema = "reunions")
public class Reunion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "session_tontine_id")
    private UUID sessionTontineId;

    @Column(name = "titre", nullable = false, length = 300)
    private String titre;

    @Column(name = "date_reunion", nullable = false)
    private LocalDateTime dateReunion;

    @Column(name = "lieu_reunion", columnDefinition = "TEXT")
    private String lieuReunion;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_siege", nullable = false, length = 20)
    private TypeSiege typeSiege = TypeSiege.FIXE;

    @Column(name = "hote_id")
    private UUID hoteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    private StatutReunion statut = StatutReunion.PLANIFIEE;

    @Column(name = "ordre_jour", columnDefinition = "TEXT")
    private String ordreJour;

    @Column(name = "compte_rendu", columnDefinition = "TEXT")
    private String compteRendu;

    @Column(name = "president_reunion_id")
    private UUID presidentReunionId;

    @Column(name = "secretaire_reunion_id")
    private UUID secretaireReunionId;

    @Column(name = "tresorier_reunion_id")
    private UUID tresorierReunionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Reunion() {
    }

    public Reunion(UUID id, UUID groupeId, UUID sessionTontineId, String titre, LocalDateTime dateReunion,
                   String lieuReunion, TypeSiege typeSiege, UUID hoteId, StatutReunion statut,
                   String ordreJour, String compteRendu, UUID presidentReunionId,
                   UUID secretaireReunionId, UUID tresorierReunionId) {
        this.id = id;
        this.groupeId = groupeId;
        this.sessionTontineId = sessionTontineId;
        this.titre = titre;
        this.dateReunion = dateReunion;
        this.lieuReunion = lieuReunion;
        this.typeSiege = typeSiege != null ? typeSiege : TypeSiege.FIXE;
        this.hoteId = hoteId;
        this.statut = statut != null ? statut : StatutReunion.PLANIFIEE;
        this.ordreJour = ordreJour;
        this.compteRendu = compteRendu;
        this.presidentReunionId = presidentReunionId;
        this.secretaireReunionId = secretaireReunionId;
        this.tresorierReunionId = tresorierReunionId;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = StatutReunion.PLANIFIEE;
        }
        if (this.typeSiege == null) {
            this.typeSiege = TypeSiege.FIXE;
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

    public UUID getSessionTontineId() {
        return sessionTontineId;
    }

    public void setSessionTontineId(UUID sessionTontineId) {
        this.sessionTontineId = sessionTontineId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public LocalDateTime getDateReunion() {
        return dateReunion;
    }

    public void setDateReunion(LocalDateTime dateReunion) {
        this.dateReunion = dateReunion;
    }

    public String getLieuReunion() {
        return lieuReunion;
    }

    public void setLieuReunion(String lieuReunion) {
        this.lieuReunion = lieuReunion;
    }

    public TypeSiege getTypeSiege() {
        return typeSiege;
    }

    public void setTypeSiege(TypeSiege typeSiege) {
        this.typeSiege = typeSiege;
    }

    public UUID getHoteId() {
        return hoteId;
    }

    public void setHoteId(UUID hoteId) {
        this.hoteId = hoteId;
    }

    public StatutReunion getStatut() {
        return statut;
    }

    public void setStatut(StatutReunion statut) {
        this.statut = statut;
    }

    public String getOrdreJour() {
        return ordreJour;
    }

    public void setOrdreJour(String ordreJour) {
        this.ordreJour = ordreJour;
    }

    public String getCompteRendu() {
        return compteRendu;
    }

    public void setCompteRendu(String compteRendu) {
        this.compteRendu = compteRendu;
    }

    public UUID getPresidentReunionId() {
        return presidentReunionId;
    }

    public void setPresidentReunionId(UUID presidentReunionId) {
        this.presidentReunionId = presidentReunionId;
    }

    public UUID getSecretaireReunionId() {
        return secretaireReunionId;
    }

    public void setSecretaireReunionId(UUID secretaireReunionId) {
        this.secretaireReunionId = secretaireReunionId;
    }

    public UUID getTresorierReunionId() {
        return tresorierReunionId;
    }

    public void setTresorierReunionId(UUID tresorierReunionId) {
        this.tresorierReunionId = tresorierReunionId;
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
