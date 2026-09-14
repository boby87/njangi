package com.njangi.membres.infrastructure.adapter.out.persistence.entity;

import com.njangi.membres.domain.model.RoleMembre;
import com.njangi.membres.domain.model.StatutMembre;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "membre_groupe", schema = "membres")
public class MembreGroupeJpaEntity {

    @Id
    private UUID id;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private RoleMembre role;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutMembre statut;

    @Column(name = "rejoint_le")
    private LocalDateTime rejointLe;

    @Column(name = "modifie_le")
    private LocalDateTime modifieLe;

    public MembreGroupeJpaEntity() {}

    public MembreGroupeJpaEntity(UUID id, UUID utilisateurId, UUID groupeId, RoleMembre role, StatutMembre statut, LocalDateTime rejointLe, LocalDateTime modifieLe) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.groupeId = groupeId;
        this.role = role;
        this.statut = statut;
        this.rejointLe = rejointLe;
        this.modifieLe = modifieLe;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(UUID utilisateurId) { this.utilisateurId = utilisateurId; }

    public UUID getGroupeId() { return groupeId; }
    public void setGroupeId(UUID groupeId) { this.groupeId = groupeId; }

    public RoleMembre getRole() { return role; }
    public void setRole(RoleMembre role) { this.role = role; }

    public StatutMembre getStatut() { return statut; }
    public void setStatut(StatutMembre statut) { this.statut = statut; }

    public LocalDateTime getRejointLe() { return rejointLe; }
    public void setRejointLe(LocalDateTime rejointLe) { this.rejointLe = rejointLe; }

    public LocalDateTime getModifieLe() { return modifieLe; }
    public void setModifieLe(LocalDateTime modifieLe) { this.modifieLe = modifieLe; }

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (rejointLe == null) rejointLe = LocalDateTime.now();
        if (modifieLe == null) modifieLe = LocalDateTime.now();
        if (role == null) role = RoleMembre.MEMBRE;
        if (statut == null) statut = StatutMembre.ACTIF;
    }

    @PreUpdate
    protected void onUpdate() {
        modifieLe = LocalDateTime.now();
    }
}
