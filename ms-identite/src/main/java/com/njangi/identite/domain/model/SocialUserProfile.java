package com.njangi.identite.domain.model;

/**
 * Profil utilisateur vérifié retourné par un fournisseur d'identité SSO (Google OIDC, etc.).
 * Value Object pur du domaine — zéro dépendance Spring/JPA.
 */
public record SocialUserProfile(
        String provider,
        String providerId,
        String email,
        boolean emailVerified,
        String nom,
        String prenom,
        String photoUrl
) {
    public SocialUserProfile {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Le fournisseur SSO est obligatoire");
        }
        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("L'identifiant unique du fournisseur SSO (sub) est obligatoire");
        }
        provider = provider.trim().toUpperCase();
        providerId = providerId.trim();
        email = (email != null && !email.isBlank()) ? email.trim().toLowerCase() : null;
        nom = (nom != null && !nom.isBlank()) ? nom.trim() : "Njangi";
        prenom = (prenom != null && !prenom.isBlank()) ? prenom.trim() : "Membre";
        photoUrl = (photoUrl != null && !photoUrl.isBlank()) ? photoUrl.trim() : null;
    }
}
