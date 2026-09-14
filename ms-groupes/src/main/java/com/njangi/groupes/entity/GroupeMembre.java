package com.njangi.groupes.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "groupe_membre", schema = "groupes")
public class GroupeMembre {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "membre_id", nullable = false)
    private UUID membreId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private RoleMembre role = RoleMembre.MEMBRE;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    private StatutMembreGroupe statut = StatutMembreGroupe.ACTIF;

    @Column(name = "date_adhesion", nullable = false)
    private LocalDateTime dateAdhesion;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    public GroupeMembre() {
    }

    public GroupeMembre(UUID id, UUID groupeId, UUID membreId, RoleMembre role,
                        StatutMembreGroupe statut, LocalDateTime dateAdhesion, LocalDateTime dateFin) {
        this.id = id;
        this.groupeId = groupeId;
        this.membreId = membreId;
        this.role = role != null ? role : RoleMembre.MEMBRE;
        this.statut = statut != null ? statut : StatutMembreGroupe.ACTIF;
        this.dateAdhesion = dateAdhesion != null ? dateAdhesion : LocalDateTime.now();
        this.dateFin = dateFin;
    }

    @PrePersist
    protected void onCreate() {
        if (this.dateAdhesion == null) {
            this.dateAdhesion = LocalDateTime.now();
        }
        if (this.statut == null) {
            this.statut = StatutMembreGroupe.ACTIF;
        }
        if (this.role == null) {
            this.role = RoleMembre.MEMBRE;
        }
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

    public RoleMembre getRole() {
        return role;
    }

    public void setRole(RoleMembre role) {
        this.role = role;
    }

    public StatutMembreGroupe getStatut() {
        return statut;
    }

    public void setStatut(StatutMembreGroupe statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateAdhesion() {
        return dateAdhesion;
    }

    public void setDateAdhesion(LocalDateTime dateAdhesion) {
        this.dateAdhesion = dateAdhesion;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }
}
