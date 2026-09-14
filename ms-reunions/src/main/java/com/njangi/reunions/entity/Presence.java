package com.njangi.reunions.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "presence", schema = "reunions")
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reunion_id", nullable = false)
    private UUID reunionId;

    @Column(name = "membre_id", nullable = false)
    private UUID membreId;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    private StatutPresence statut = StatutPresence.ABSENT;

    @Column(name = "heure_arrivee")
    private LocalDateTime heureArrivee;

    @Column(name = "justification", columnDefinition = "TEXT")
    private String justification;

    @Column(name = "procuration", nullable = false)
    private Boolean procuration = false;

    @Column(name = "mandataire_id")
    private UUID mandataireId;

    @Column(name = "enregistre_le", nullable = false)
    private LocalDateTime enregistreLe;

    public Presence() {
    }

    public Presence(UUID id, UUID reunionId, UUID membreId, StatutPresence statut,
                    LocalDateTime heureArrivee, String justification, Boolean procuration,
                    UUID mandataireId, LocalDateTime enregistreLe) {
        this.id = id;
        this.reunionId = reunionId;
        this.membreId = membreId;
        this.statut = statut != null ? statut : StatutPresence.ABSENT;
        this.heureArrivee = heureArrivee;
        this.justification = justification;
        this.procuration = procuration != null ? procuration : false;
        this.mandataireId = mandataireId;
        this.enregistreLe = enregistreLe != null ? enregistreLe : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.enregistreLe == null) {
            this.enregistreLe = LocalDateTime.now();
        }
        if (this.statut == null) {
            this.statut = StatutPresence.ABSENT;
        }
        if (this.procuration == null) {
            this.procuration = false;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReunionId() {
        return reunionId;
    }

    public void setReunionId(UUID reunionId) {
        this.reunionId = reunionId;
    }

    public UUID getMembreId() {
        return membreId;
    }

    public void setMembreId(UUID membreId) {
        this.membreId = membreId;
    }

    public StatutPresence getStatut() {
        return statut;
    }

    public void setStatut(StatutPresence statut) {
        this.statut = statut;
    }

    public LocalDateTime getHeureArrivee() {
        return heureArrivee;
    }

    public void setHeureArrivee(LocalDateTime heureArrivee) {
        this.heureArrivee = heureArrivee;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public Boolean getProcuration() {
        return procuration;
    }

    public void setProcuration(Boolean procuration) {
        this.procuration = procuration;
    }

    public UUID getMandataireId() {
        return mandataireId;
    }

    public void setMandataireId(UUID mandataireId) {
        this.mandataireId = mandataireId;
    }

    public LocalDateTime getEnregistreLe() {
        return enregistreLe;
    }

    public void setEnregistreLe(LocalDateTime enregistreLe) {
        this.enregistreLe = enregistreLe;
    }
}
