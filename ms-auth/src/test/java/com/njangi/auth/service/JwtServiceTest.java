package com.njangi.auth.service;

import com.njangi.auth.entity.StatutUtilisateur;
import com.njangi.auth.entity.Utilisateur;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("NjangiSecretKeyForTestingHMACSHA256TokensMustBe256BitsLong!", 3600000);
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
}
