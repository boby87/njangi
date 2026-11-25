package com.njangi.identite.domain.port.out;

import com.njangi.identite.domain.model.SocialUserProfile;

/**
 * Port sortant (Architecture Hexagonale) chargé de vérifier cryptographiquement un jeton SSO
 * auprès du fournisseur d'identité (ex. Google OIDC JWKS) et d'en extraire les claims certifiés.
 */
public interface SocialTokenVerifierPort {

    SocialUserProfile verifierEtExtraire(
            String provider,
            String token,
            String fallbackProviderId,
            String fallbackEmail,
            String fallbackNom,
            String fallbackPrenom,
            String fallbackPhotoUrl
    );

    boolean isGoogleConfigured();

    String getPrimaryGoogleClientId();

    boolean isDevFallbackEnabled();
}
