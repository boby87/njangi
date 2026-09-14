package com.njangi.membres.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class AdhesionGroupe {
    private final UUID id;
    private final UUID utilisateurId;
    private final UUID groupeId;
    private RoleMembre role;
    private StatutMembre statut;
    private final LocalDateTime rejointLe;
    private LocalDateTime modifieLe;

    public AdhesionGroupe(UUID id, UUID utilisateurId, UUID groupeId, RoleMembre role, StatutMembre statut, LocalDateTime rejointLe, LocalDateTime modifieLe) {
        this.id = id != null ? id : UUID.randomUUID();
        this.utilisateurId = utilisateurId;
        this.groupeId = groupeId;
        this.role = role != null ? role : RoleMembre.MEMBRE;
        this.statut = statut != null ? statut : StatutMembre.ACTIF;
        this.rejointLe = rejointLe != null ? rejointLe : LocalDateTime.now();
        this.modifieLe = modifieLe != null ? modifieLe : LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getUtilisateurId() { return utilisateurId; }
    public UUID getGroupeId() { return groupeId; }
    public RoleMembre getRole() { return role; }
    public StatutMembre getStatut() { return statut; }
    public LocalDateTime getRejointLe() { return rejointLe; }
    public LocalDateTime getModifieLe() { return modifieLe; }

    public void changerRole(RoleMembre nouveauRole) {
        this.role = nouveauRole;
        this.modifieLe = LocalDateTime.now();
    }

    public void suspendre() {
        this.statut = StatutMembre.SUSPENDU;
        this.modifieLe = LocalDateTime.now();
    }

    public void reactiver() {
        this.statut = StatutMembre.ACTIF;
        this.modifieLe = LocalDateTime.now();
    }
}
