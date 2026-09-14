package com.njangi.paiements.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "paiement", schema = "paiements")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cotisation_id", nullable = false)
    private UUID cotisationId;

    @Column(name = "membre_id", nullable = false)
    private UUID membreId;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "montant", nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", nullable = false, length = 50)
    private ModePaiement modePaiement;

    @Column(name = "cle_idempotence", nullable = false, unique = true, length = 150)
    private String cleIdempotence;

    @Column(name = "reference", unique = true, length = 100)
    private String reference;

    @Column(name = "piece_jointe_url", length = 500)
    private String pieceJointeUrl;

    @Column(name = "numero_telephone", length = 50)
    private String numeroTelephone;

    @Column(name = "operateur", length = 50)
    private String operateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    private StatutPaiement statut = StatutPaiement.EN_ATTENTE_VALIDATION;

    @Column(name = "valide_par")
    private UUID validePar;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_paiement", nullable = false)
    private LocalDateTime datePaiement;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Paiement() {
    }

    public Paiement(UUID id, UUID cotisationId, UUID membreId, UUID groupeId,
                    BigDecimal montant, ModePaiement modePaiement, String cleIdempotence,
                    String reference, String pieceJointeUrl, String numeroTelephone,
                    String operateur, StatutPaiement statut) {
        this.id = id;
        this.cotisationId = cotisationId;
        this.membreId = membreId;
        this.groupeId = groupeId;
        this.montant = montant;
        this.modePaiement = modePaiement;
        this.cleIdempotence = cleIdempotence;
        this.reference = reference;
        this.pieceJointeUrl = pieceJointeUrl;
        this.numeroTelephone = numeroTelephone;
        this.operateur = operateur;
        this.statut = statut != null ? statut : StatutPaiement.EN_ATTENTE_VALIDATION;
        this.datePaiement = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (datePaiement == null) {
            datePaiement = LocalDateTime.now();
        }
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

    public UUID getCotisationId() {
        return cotisationId;
    }

    public void setCotisationId(UUID cotisationId) {
        this.cotisationId = cotisationId;
    }

    public UUID getMembreId() {
        return membreId;
    }

    public void setMembreId(UUID membreId) {
        this.membreId = membreId;
    }

    public UUID getGroupeId() {
        return groupeId;
    }

    public void setGroupeId(UUID groupeId) {
        this.groupeId = groupeId;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public ModePaiement getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(ModePaiement modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getCleIdempotence() {
        return cleIdempotence;
    }

    public void setCleIdempotence(String cleIdempotence) {
        this.cleIdempotence = cleIdempotence;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPieceJointeUrl() {
        return pieceJointeUrl;
    }

    public void setPieceJointeUrl(String pieceJointeUrl) {
        this.pieceJointeUrl = pieceJointeUrl;
    }

    public String getNumeroTelephone() {
        return numeroTelephone;
    }

    public void setNumeroTelephone(String numeroTelephone) {
        this.numeroTelephone = numeroTelephone;
    }

    public String getOperateur() {
        return operateur;
    }

    public void setOperateur(String operateur) {
        this.operateur = operateur;
    }

    public StatutPaiement getStatut() {
        return statut;
    }

    public void setStatut(StatutPaiement statut) {
        this.statut = statut;
    }

    public UUID getValidePar() {
        return validePar;
    }

    public void setValidePar(UUID validePar) {
        this.validePar = validePar;
    }

    public LocalDateTime getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDateTime dateValidation) {
        this.dateValidation = dateValidation;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
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
        if (!(o instanceof Paiement that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
