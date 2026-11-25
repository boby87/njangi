package com.njangi.identite.infrastructure.adapter.out.sso;

import com.njangi.identite.domain.exception.BusinessException;
import com.njangi.identite.domain.model.SocialUserProfile;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class GoogleOidcTokenVerifierAdapterTest {

    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;
    private static JwtDecoder rsaDecoder;

    private static final String TEST_CLIENT_ID = "1234567890-testweb.apps.googleusercontent.com";

    @BeforeAll
    static void generateRsaKeys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        rsaDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    private SsoProperties configuredProperties(boolean allowDevFallback) {
        SsoProperties props = new SsoProperties();
        props.setAllowDevFallback(allowDevFallback);
        props.getGoogle().setWebClientId(TEST_CLIENT_ID);
        return props;
    }

    private String signGoogleToken(String issuer, String audience, Instant expiresAt, boolean emailVerified) {
        return Jwts.builder()
                .subject("109876543210987654321")
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("email", "jeanpaul.mbarga@gmail.com")
                .claim("email_verified", emailVerified)
                .claim("given_name", "Jean-Paul")
                .claim("family_name", "Mbarga")
                .claim("picture", "https://lh3.googleusercontent.com/a/photo")
                .issuedAt(Date.from(Instant.now().minusSeconds(10)))
                .expiration(Date.from(expiresAt))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    @Test
    @DisplayName("Valide un vrai Google ID Token RS256 et extrait les claims certifiés")
    void testValidGoogleIdToken() {
        GoogleOidcTokenVerifierAdapter adapter = new GoogleOidcTokenVerifierAdapter(
                configuredProperties(false), rsaDecoder
        );

        String idToken = signGoogleToken("https://accounts.google.com", TEST_CLIENT_ID, Instant.now().plusSeconds(3600), true);

        SocialUserProfile profile = adapter.verifierEtExtraire(
                "GOOGLE", idToken, "spoofed-sub", "spoofed@evil.com", "Evil", "Hacker", null
        );

        assertEquals("GOOGLE", profile.provider());
        assertEquals("109876543210987654321", profile.providerId());
        assertEquals("jeanpaul.mbarga@gmail.com", profile.email());
        assertTrue(profile.emailVerified());
        assertEquals("Mbarga", profile.nom());
        assertEquals("Jean-Paul", profile.prenom());
        assertEquals("https://lh3.googleusercontent.com/a/photo", profile.photoUrl());
    }

    @Test
    @DisplayName("Rejette un Google ID Token émis pour une autre audience (protection confused deputy)")
    void testInvalidAudienceRejected() {
        GoogleOidcTokenVerifierAdapter adapter = new GoogleOidcTokenVerifierAdapter(
                configuredProperties(false), rsaDecoder
        );

        String idToken = signGoogleToken(
                "https://accounts.google.com",
                "autre-application-malveillante.apps.googleusercontent.com",
                Instant.now().plusSeconds(3600),
                true
        );

        BusinessException ex = assertThrows(BusinessException.class, () ->
                adapter.verifierEtExtraire("GOOGLE", idToken, null, null, null, null, null)
        );
        assertTrue(ex.getMessage().contains("Njangi"));
    }

    @Test
    @DisplayName("Rejette un Google ID Token avec un émetteur (iss) non autorisé")
    void testInvalidIssuerRejected() {
        GoogleOidcTokenVerifierAdapter adapter = new GoogleOidcTokenVerifierAdapter(
                configuredProperties(false), rsaDecoder
        );

        String idToken = signGoogleToken(
                "https://evil-issuer.example.com",
                TEST_CLIENT_ID,
                Instant.now().plusSeconds(3600),
                true
        );

        assertThrows(BusinessException.class, () ->
                adapter.verifierEtExtraire("GOOGLE", idToken, null, null, null, null, null)
        );
    }

    @Test
    @DisplayName("Rejette un Google ID Token expiré")
    void testExpiredTokenRejected() {
        GoogleOidcTokenVerifierAdapter adapter = new GoogleOidcTokenVerifierAdapter(
                configuredProperties(false), rsaDecoder
        );

        String expiredToken = signGoogleToken(
                "https://accounts.google.com",
                TEST_CLIENT_ID,
                Instant.now().minusSeconds(300),
                true
        );

        assertThrows(BusinessException.class, () ->
                adapter.verifierEtExtraire("GOOGLE", expiredToken, null, null, null, null, null)
        );
    }

    @Test
    @DisplayName("Rejette l'absence de token quand GOOGLE_CLIENT_ID est configuré")
    void testMissingTokenRejectedWhenConfigured() {
        GoogleOidcTokenVerifierAdapter adapter = new GoogleOidcTokenVerifierAdapter(
                configuredProperties(true), rsaDecoder
        );

        assertThrows(BusinessException.class, () ->
                adapter.verifierEtExtraire("GOOGLE", "", "dev-id", "dev@njangi.cm", "Dev", "User", null)
        );
    }

    @Test
    @DisplayName("Rejette le fallback dev par défaut (allowDevFallback=false) quand aucun clientId n'est défini")
    void testDevFallbackDisabledByDefault() {
        SsoProperties props = new SsoProperties();
        props.setAllowDevFallback(false);
        GoogleOidcTokenVerifierAdapter adapter = new GoogleOidcTokenVerifierAdapter(props, rsaDecoder);

        assertFalse(adapter.isGoogleConfigured());
        assertThrows(BusinessException.class, () ->
                adapter.verifierEtExtraire("GOOGLE", "", "dev-id", "dev@njangi.cm", "Dev", "User", null)
        );
    }

    @Test
    @DisplayName("Autorise le fallback dev explicite (allowDevFallback=true) sans GOOGLE_CLIENT_ID")
    void testDevFallbackAllowedWhenExplicitlyEnabled() {
        SsoProperties props = new SsoProperties();
        props.setAllowDevFallback(true);
        GoogleOidcTokenVerifierAdapter adapter = new GoogleOidcTokenVerifierAdapter(props, rsaDecoder);

        SocialUserProfile profile = adapter.verifierEtExtraire(
                "GOOGLE", "", "dev-google-1", "dev@njangi.cm", "Njangi", "Test", null
        );

        assertEquals("GOOGLE", profile.provider());
        assertEquals("dev-google-1", profile.providerId());
        assertEquals("dev@njangi.cm", profile.email());
    }
}
