package com.njangi.identite.infrastructure.adapter.out.sso;

import com.njangi.identite.domain.exception.BusinessException;
import com.njangi.identite.domain.model.SocialUserProfile;
import com.njangi.identite.domain.port.out.SocialTokenVerifierPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Adaptateur sortant vérifiant cryptographiquement les Google ID Tokens (OIDC JWT RS256)
 * via le jeu de clés publiques JWKS de Google (https://www.googleapis.com/oauth2/v3/certs).
 */
@Component
@EnableConfigurationProperties(SsoProperties.class)
public class GoogleOidcTokenVerifierAdapter implements SocialTokenVerifierPort {

    private static final Logger log = LoggerFactory.getLogger(GoogleOidcTokenVerifierAdapter.class);

    private final SsoProperties properties;
    private volatile JwtDecoder googleJwtDecoder;

    @Autowired
    public GoogleOidcTokenVerifierAdapter(SsoProperties properties) {
        this.properties = properties;
    }

    /**
     * Constructeur dédié aux tests unitaires permettant d'injecter un JwtDecoder RSA en mémoire.
     */
    public GoogleOidcTokenVerifierAdapter(SsoProperties properties, JwtDecoder customGoogleDecoder) {
        this.properties = properties;
        this.googleJwtDecoder = customGoogleDecoder;
    }

    @Override
    public SocialUserProfile verifierEtExtraire(
            String provider,
            String token,
            String fallbackProviderId,
            String fallbackEmail,
            String fallbackNom,
            String fallbackPrenom,
            String fallbackPhotoUrl
    ) {
        String normalizedProvider = provider == null ? "" : provider.trim().toUpperCase(Locale.ROOT);
        if (!"GOOGLE".equals(normalizedProvider) && !"FACEBOOK".equals(normalizedProvider)) {
            throw new BusinessException("Fournisseur SSO non supporté : " + provider + ". Utilisez 'GOOGLE'.");
        }

        if ("GOOGLE".equals(normalizedProvider)) {
            return verifyGoogle(token, fallbackProviderId, fallbackEmail, fallbackNom, fallbackPrenom, fallbackPhotoUrl);
        }

        return handleDevFallbackOnly(
                normalizedProvider,
                fallbackProviderId,
                fallbackEmail,
                fallbackNom,
                fallbackPrenom,
                fallbackPhotoUrl
        );
    }

    @Override
    public boolean isGoogleConfigured() {
        return !properties.getGoogle().resolveAllClientIds().isEmpty();
    }

    @Override
    public String getPrimaryGoogleClientId() {
        return properties.getGoogle().resolvePrimaryWebClientId();
    }

    @Override
    public boolean isDevFallbackEnabled() {
        return properties.isAllowDevFallback();
    }

    private SocialUserProfile verifyGoogle(
            String idToken,
            String fallbackProviderId,
            String fallbackEmail,
            String fallbackNom,
            String fallbackPrenom,
            String fallbackPhotoUrl
    ) {
        List<String> allowedClientIds = properties.getGoogle().resolveAllClientIds();
        boolean hasToken = idToken != null && !idToken.isBlank();

        // Si un token est fourni et Google est configuré (ou même sans clientId en mode strict) → vérification cryptographique
        if (hasToken && (!allowedClientIds.isEmpty() || !properties.isAllowDevFallback())) {
            if (allowedClientIds.isEmpty()) {
                throw new BusinessException(
                        "Google Sign-In n'est pas encore configuré sur le serveur (variable GOOGLE_CLIENT_ID manquante)."
                );
            }
            return decodeAndValidateGoogleIdToken(idToken.trim(), allowedClientIds);
        }

        // Si Google est configuré, exiger impérativement un vrai ID token Google
        if (!allowedClientIds.isEmpty()) {
            throw new BusinessException("Le jeton Google ID Token (credential) est obligatoire.");
        }

        // Aucun GOOGLE_CLIENT_ID configuré : fallback autorisé uniquement si allowDevFallback=true
        return handleDevFallbackOnly(
                "GOOGLE",
                fallbackProviderId,
                fallbackEmail,
                fallbackNom,
                fallbackPrenom,
                fallbackPhotoUrl
        );
    }

    private SocialUserProfile decodeAndValidateGoogleIdToken(String idToken, List<String> allowedClientIds) {
        Jwt jwt;
        try {
            jwt = getOrCreateGoogleDecoder().decode(idToken);
        } catch (JwtException ex) {
            log.warn("Échec de validation cryptographique du Google ID Token : {}", ex.getMessage());
            throw new BusinessException("Jeton Google invalide ou expiré. Veuillez vous reconnecter via Google.");
        }

        // 1. Validation de l'émetteur (iss)
        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : jwt.getClaimAsString("iss");
        List<String> allowedIssuers = properties.getGoogle().getIssuers();
        if (issuer == null || !allowedIssuers.contains(issuer)) {
            log.warn("Issuer Google non autorisé : {}", issuer);
            throw new BusinessException("Émetteur du jeton Google non autorisé.");
        }

        // 2. Validation stricte de l'audience (aud) contre les Client IDs Njangi (Web / Android)
        List<String> audiences = jwt.getAudience();
        boolean audienceValide = audiences != null && audiences.stream().anyMatch(allowedClientIds::contains);
        if (!audienceValide) {
            log.warn("Audience Google invalide : aud={}, attendus={}", audiences, allowedClientIds);
            throw new BusinessException("Ce jeton Google n'a pas été émis pour l'application Njangi.");
        }

        // 3. Extraction sécurisée des claims certifiés par Google
        String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new BusinessException("Identifiant utilisateur (sub) absent du jeton Google.");
        }

        String email = jwt.getClaimAsString("email");
        Boolean emailVerifiedClaim = jwt.getClaim("email_verified");
        boolean emailVerified = Boolean.TRUE.equals(emailVerifiedClaim);

        String givenName = jwt.getClaimAsString("given_name");
        String familyName = jwt.getClaimAsString("family_name");
        String fullName = jwt.getClaimAsString("name");

        if ((givenName == null || givenName.isBlank()) && fullName != null && !fullName.isBlank()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            givenName = parts[0];
            if ((familyName == null || familyName.isBlank()) && parts.length > 1) {
                familyName = parts[1];
            }
        }

        String picture = jwt.getClaimAsString("picture");

        return new SocialUserProfile(
                "GOOGLE",
                sub,
                email,
                emailVerified,
                familyName != null ? familyName : "Njangi",
                givenName != null ? givenName : "Membre",
                picture
        );
    }

    private SocialUserProfile handleDevFallbackOnly(
            String provider,
            String fallbackProviderId,
            String fallbackEmail,
            String fallbackNom,
            String fallbackPrenom,
            String fallbackPhotoUrl
    ) {
        if (!properties.isAllowDevFallback()) {
            throw new BusinessException(
                    "Authentification SSO " + provider + " non configurée. Définissez GOOGLE_CLIENT_ID sur le serveur."
            );
        }
        if (fallbackProviderId == null || fallbackProviderId.isBlank()) {
            throw new BusinessException("Identifiant fournisseur (providerId) ou jeton SSO obligatoire.");
        }

        log.warn("Mode DEV SSO actif (njangi.sso.allow-dev-fallback=true) : authentification {} simulée pour {}",
                provider, fallbackProviderId);

        return new SocialUserProfile(
                provider,
                fallbackProviderId,
                fallbackEmail,
                fallbackEmail != null && !fallbackEmail.isBlank(),
                fallbackNom,
                fallbackPrenom,
                fallbackPhotoUrl
        );
    }

    private JwtDecoder getOrCreateGoogleDecoder() {
        JwtDecoder decoder = this.googleJwtDecoder;
        if (decoder == null) {
            synchronized (this) {
                decoder = this.googleJwtDecoder;
                if (decoder == null) {
                    decoder = NimbusJwtDecoder.withJwkSetUri(properties.getGoogle().getJwkSetUri()).build();
                    this.googleJwtDecoder = decoder;
                }
            }
        }
        return decoder;
    }
}
