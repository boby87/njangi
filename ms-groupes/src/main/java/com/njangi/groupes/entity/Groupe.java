package com.njangi.groupes.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "groupe", schema = "groupes")
public class Groupe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nom", nullable = false, length = 200)
    private String nom;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "createur_membre_id", nullable = false)
    private UUID createurMembreId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_siege", nullable = false, length = 20)
    private TypeSiege typeSiege = TypeSiege.FIXE;

    @Column(name = "adresse_siege", columnDefinition = "TEXT")
    private String adresseSiege;

    @Column(name = "montant_cotisation_principale", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantCotisationPrincipale;

    @Column(name = "frequence_reunion", nullable = false, length = 50)
    private String frequenceReunion;

    @Column(name = "nombre_membres_max")
    private Integer nombreMembresMax;

    @Column(name = "code_invitation", unique = true, nullable = false, length = 50)
    private String codeInvitation;

    @Column(name = "reglement_interieur", columnDefinition = "TEXT")
    private String reglementInterieur;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    private StatutGroupe statut = StatutGroupe.ACTIF;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Groupe() {
    }

    public Groupe(UUID id, String nom, String description, UUID createurMembreId,
                  TypeSiege typeSiege, String adresseSiege, BigDecimal montantCotisationPrincipale,
                  String frequenceReunion, Integer nombreMembresMax, String codeInvitation,
                  String reglementInterieur, StatutGroupe statut) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.createurMembreId = createurMembreId;
        this.typeSiege = typeSiege != null ? typeSiege : TypeSiege.FIXE;
        this.adresseSiege = adresseSiege;
        this.montantCotisationPrincipale = montantCotisationPrincipale;
        this.frequenceReunion = frequenceReunion;
        this.nombreMembresMax = nombreMembresMax;
        this.codeInvitation = codeInvitation;
        this.reglementInterieur = reglementInterieur;
        this.statut = statut != null ? statut : StatutGroupe.ACTIF;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = StatutGroupe.ACTIF;
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

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getCreateurMembreId() {
        return createurMembreId;
    }

    public void setCreateurMembreId(UUID createurMembreId) {
        this.createurMembreId = createurMembreId;
    }

    public TypeSiege getTypeSiege() {
        return typeSiege;
    }

    public void setTypeSiege(TypeSiege typeSiege) {
        this.typeSiege = typeSiege;
    }

    public String getAdresseSiege() {
        return adresseSiege;
    }

    public void setAdresseSiege(String adresseSiege) {
        this.adresseSiege = adresseSiege;
    }

    public BigDecimal getMontantCotisationPrincipale() {
        return montantCotisationPrincipale;
    }

    public void setMontantCotisationPrincipale(BigDecimal montantCotisationPrincipale) {
        this.montantCotisationPrincipale = montantCotisationPrincipale;
    }

    public String getFrequenceReunion() {
        return frequenceReunion;
    }

    public void setFrequenceReunion(String frequenceReunion) {
        this.frequenceReunion = frequenceReunion;
    }

    public Integer getNombreMembresMax() {
        return nombreMembresMax;
    }

    public void setNombreMembresMax(Integer nombreMembresMax) {
        this.nombreMembresMax = nombreMembresMax;
    }

    public String getCodeInvitation() {
        return codeInvitation;
    }

    public void setCodeInvitation(String codeInvitation) {
        this.codeInvitation = codeInvitation;
    }

    public String getReglementInterieur() {
        return reglementInterieur;
    }

    public void setReglementInterieur(String reglementInterieur) {
        this.reglementInterieur = reglementInterieur;
    }

    public StatutGroupe getStatut() {
        return statut;
    }

    public void setStatut(StatutGroupe statut) {
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
