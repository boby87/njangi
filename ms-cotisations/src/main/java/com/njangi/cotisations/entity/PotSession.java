package com.njangi.cotisations.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "pot_session", schema = "cotisations")
public class PotSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "session_tontine_id", nullable = false)
    private UUID sessionTontineId;

    @Column(name = "reunion_id")
    private UUID reunionId;

    @Column(name = "membre_beneficiaire_id", nullable = false)
    private UUID membreBeneficiaireId;

    @Column(name = "ordre_passage", nullable = false)
    private int ordrePassage = 1;

    @Column(name = "montant_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTotal;

    @Column(name = "montant_net", precision = 15, scale = 2)
    private BigDecimal montantNet;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    private StatutPot statut = StatutPot.PLANIFIE;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_versement", length = 50)
    private ModeVersement modeVersement;

    @Column(name = "reference_paiement", length = 150)
    private String referencePaiement;

    @Column(name = "date_attribution")
    private LocalDateTime dateAttribution;

    @Column(name = "date_versement")
    private LocalDateTime dateVersement;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PotSession() {
    }

    public PotSession(UUID id, UUID groupeId, UUID sessionTontineId, UUID reunionId,
                      UUID membreBeneficiaireId, int ordrePassage, BigDecimal montantTotal,
                      BigDecimal montantNet, StatutPot statut) {
        this.id = id;
        this.groupeId = groupeId;
        this.sessionTontineId = sessionTontineId;
        this.reunionId = reunionId;
        this.membreBeneficiaireId = membreBeneficiaireId;
        this.ordrePassage = ordrePassage;
        this.montantTotal = montantTotal;
        this.montantNet = montantNet != null ? montantNet : montantTotal;
        this.statut = statut != null ? statut : StatutPot.PLANIFIE;
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

    public UUID getSessionTontineId() {
        return sessionTontineId;
    }

    public void setSessionTontineId(UUID sessionTontineId) {
        this.sessionTontineId = sessionTontineId;
    }

    public UUID getReunionId() {
        return reunionId;
    }

    public void setReunionId(UUID reunionId) {
        this.reunionId = reunionId;
    }

    public UUID getMembreBeneficiaireId() {
        return membreBeneficiaireId;
    }

    public void setMembreBeneficiaireId(UUID membreBeneficiaireId) {
        this.membreBeneficiaireId = membreBeneficiaireId;
    }

    public int getOrdrePassage() {
        return ordrePassage;
    }

    public void setOrdrePassage(int ordrePassage) {
        this.ordrePassage = ordrePassage;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public BigDecimal getMontantNet() {
        return montantNet;
    }

    public void setMontantNet(BigDecimal montantNet) {
        this.montantNet = montantNet;
    }

    public StatutPot getStatut() {
        return statut;
    }

    public void setStatut(StatutPot statut) {
        this.statut = statut;
    }

    public ModeVersement getModeVersement() {
        return modeVersement;
    }

    public void setModeVersement(ModeVersement modeVersement) {
        this.modeVersement = modeVersement;
    }

    public String getReferencePaiement() {
        return referencePaiement;
    }

    public void setReferencePaiement(String referencePaiement) {
        this.referencePaiement = referencePaiement;
    }

    public LocalDateTime getDateAttribution() {
        return dateAttribution;
    }

    public void setDateAttribution(LocalDateTime dateAttribution) {
        this.dateAttribution = dateAttribution;
    }

    public LocalDateTime getDateVersement() {
        return dateVersement;
    }

    public void setDateVersement(LocalDateTime dateVersement) {
        this.dateVersement = dateVersement;
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
        if (!(o instanceof PotSession that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
