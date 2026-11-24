package com.njangi.identite.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Utilisateur {

    private UUID id;
    private String telephone;
    private String email;
    private String motDePasseHash;
    private String nom;
    private String prenom;
    private String photoUrl;
    private String ville;
    private String pays;
    private StatutUtilisateur statut;
    private String providerAuth;
    private String providerId;
    private LocalDateTime creeLe;
    private LocalDateTime modifieLe;

    public Utilisateur() {
        this.providerAuth = "LOCAL";
        this.statut = StatutUtilisateur.EN_ATTENTE_VERIFICATION;
    }

    public Utilisateur(
            UUID id,
            String telephone,
            String email,
            String motDePasseHash,
            String nom,
            String prenom,
            String photoUrl,
            String ville,
            String pays,
            StatutUtilisateur statut,
            String providerAuth,
            String providerId,
            LocalDateTime creeLe,
            LocalDateTime modifieLe
    ) {
        this.id = id;
        this.telephone = telephone;
        this.email = email;
        this.motDePasseHash = motDePasseHash;
        this.nom = nom;
        this.prenom = prenom;
        this.photoUrl = photoUrl;
        this.ville = ville;
        this.pays = pays;
        this.statut = statut != null ? statut : StatutUtilisateur.EN_ATTENTE_VERIFICATION;
        this.providerAuth = providerAuth != null ? providerAuth : "LOCAL";
        this.providerId = providerId;
        this.creeLe = creeLe;
        this.modifieLe = modifieLe;
    }

    public String getNomComplet() {
        String n = nom != null ? nom : "";
        String p = prenom != null ? prenom : "";
        return (n + " " + p).trim();
    }

    public boolean isSuspendu() {
        return statut == StatutUtilisateur.SUSPENDU;
    }

    public boolean needsTelephone() {
        return telephone == null || telephone.isBlank();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public void setMotDePasseHash(String motDePasseHash) {
        this.motDePasseHash = motDePasseHash;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public StatutUtilisateur getStatut() {
        return statut;
    }

    public void setStatut(StatutUtilisateur statut) {
        this.statut = statut;
    }

    public String getProviderAuth() {
        return providerAuth;
    }

    public void setProviderAuth(String providerAuth) {
        this.providerAuth = providerAuth;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public LocalDateTime getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(LocalDateTime creeLe) {
        this.creeLe = creeLe;
    }

    public LocalDateTime getModifieLe() {
        return modifieLe;
    }

    public void setModifieLe(LocalDateTime modifieLe) {
        this.modifieLe = modifieLe;
    }
}
