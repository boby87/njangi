package com.njangi.membres.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Membre {
    private final MembreId id;
    private final UUID authUtilisateurId;
    private String nom;
    private String prenom;
    private Email email;
    private Telephone telephone;
    private LocalDate dateNaissance;
    private String adresse;
    private String ville;
    private String photoUrl;
    private StatutMembre statut;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<AdhesionGroupe> adhesions;

    public Membre(MembreId id, UUID authUtilisateurId, String nom, String prenom,
                  Email email, Telephone telephone, LocalDate dateNaissance,
                  String adresse, String ville, String photoUrl,
                  StatutMembre statut, LocalDateTime createdAt, LocalDateTime updatedAt,
                  List<AdhesionGroupe> adhesions) {
        this.id = id != null ? id : MembreId.generate();
        this.authUtilisateurId = authUtilisateurId;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.dateNaissance = dateNaissance;
        this.adresse = adresse;
        this.ville = ville;
        this.photoUrl = photoUrl;
        this.statut = statut != null ? statut : StatutMembre.ACTIF;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.adhesions = adhesions != null ? new ArrayList<>(adhesions) : new ArrayList<>();
    }

    public MembreId getId() { return id; }
    public UUID getAuthUtilisateurId() { return authUtilisateurId; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public Email getEmail() { return email; }
    public Telephone getTelephone() { return telephone; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public String getAdresse() { return adresse; }
    public String getVille() { return ville; }
    public String getPhotoUrl() { return photoUrl; }
    public StatutMembre getStatut() { return statut; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<AdhesionGroupe> getAdhesions() { return Collections.unmodifiableList(adhesions); }

    public void mettreAJourProfil(String nom, String prenom, String adresse, String ville, String photoUrl) {
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.ville = ville;
        if (photoUrl != null) this.photoUrl = photoUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void suspendre() {
        this.statut = StatutMembre.SUSPENDU;
        this.updatedAt = LocalDateTime.now();
    }

    public void reactiver() {
        this.statut = StatutMembre.ACTIF;
        this.updatedAt = LocalDateTime.now();
    }

    public void radier() {
        this.statut = StatutMembre.RADIE;
        this.updatedAt = LocalDateTime.now();
    }

    public void ajouterAdhesion(AdhesionGroupe adhesion) {
        this.adhesions.add(adhesion);
        this.updatedAt = LocalDateTime.now();
    }
}
