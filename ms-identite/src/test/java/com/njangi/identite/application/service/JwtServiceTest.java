package com.njangi.identite.application.service;

import com.njangi.identite.domain.model.StatutUtilisateur;
import com.njangi.identite.domain.model.Utilisateur;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String TEST_SECRET = "NjangiSecretKeyForTestingHMACSHA256TokensMustBe256BitsLong!";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, 3600000);
    }

    @Test
    @DisplayName("Génère un jeton JWT valide et extrait les claims avec succès")
    void testGenererEtValiderToken() {
        UUID userId = UUID.randomUUID();
        Utilisateur user = new Utilisateur();
        user.setId(userId);
        user.setTelephone("+237699123456");
        user.setEmail("test@njangi.cm");
        user.setNom("Mbarga");
        user.setPrenom("Paul");
        user.setStatut(StatutUtilisateur.ACTIF);

        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));

        Claims claims = jwtService.parseAndValidateClaims(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("+237699123456", claims.get("telephone"));
        assertEquals("test@njangi.cm", claims.get("email"));
        assertEquals(userId, jwtService.extractUserId(token));
    }

    @Test
    @DisplayName("Génère un jeton pour un numéro international (Diaspora)")
    void testTokenDiaspora() {
        UUID userId = UUID.randomUUID();
        Utilisateur user = new Utilisateur();
        user.setId(userId);
        user.setTelephone("+33612345678");
        user.setEmail("diaspora.paris@njangi.cm");
        user.setNom("Kamga");
        user.setPrenom("Eric");
        user.setStatut(StatutUtilisateur.ACTIF);

        String token = jwtService.generateToken(user);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals("+33612345678", jwtService.parseAndValidateClaims(token).get("telephone"));
    }

    @Test
    @DisplayName("Le jeton signé en HS256 est décodable par NimbusJwtDecoder (compatibilité API Gateway)")
    void testCompatibiliteNimbusJwtDecoderHS256() {
        UUID userId = UUID.randomUUID();
        Utilisateur user = new Utilisateur();
        user.setId(userId);
        user.setTelephone("+237690112233");
        user.setEmail("gateway@njangi.cm");
        user.setNom("Fokou");
        user.setPrenom("Paul");
        user.setStatut(StatutUtilisateur.ACTIF);

        String token = jwtService.generateToken(user);

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(new SecretKeySpec(TEST_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        Jwt decoded = decoder.decode(token);
        assertEquals(userId.toString(), decoded.getSubject());
        assertEquals("+237690112233", decoded.getClaimAsString("telephone"));
        assertEquals("gateway@njangi.cm", decoded.getClaimAsString("email"));
    }

    @Test
    @DisplayName("Rejette un jeton altéré ou invalide")
    void testRejeterTokenInvalide() {
        assertFalse(jwtService.isTokenValid("invalid.jwt.token"));
        assertThrows(IllegalArgumentException.class, () -> jwtService.parseAndValidateClaims("invalid.jwt.token"));
    }
}
