package com.njangi.auth.service;

import com.njangi.auth.entity.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final long expiration;

    public JwtService(
            @Value("${njangi.jwt.secret:NjangiPlateformeSecreteTresLonguePourHmacSha256Signature2026!}") String secret,
            @Value("${njangi.jwt.expiration:86400000}") long expiration
    ) {
        // Garantir une clé 256-bit valide
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    public String generateToken(Utilisateur utilisateur) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(utilisateur.getId().toString())
                .claim("telephone", utilisateur.getTelephone())
                .claim("email", utilisateur.getEmail() != null ? utilisateur.getEmail() : "")
                .claim("nom", utilisateur.getNom() != null ? utilisateur.getNom() : "")
                .claim("prenom", utilisateur.getPrenom() != null ? utilisateur.getPrenom() : "")
                .claim("statut", utilisateur.getStatut() != null ? utilisateur.getStatut().name() : "ACTIF")
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .signWith(key)
                .compact();
    }

    public Claims parseAndValidateClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            log.warn("Jeton JWT expiré : {}", ex.getMessage());
            throw new IllegalArgumentException("Le jeton d'authentification a expiré");
        } catch (JwtException ex) {
            log.warn("Jeton JWT invalide : {}", ex.getMessage());
            throw new IllegalArgumentException("Jeton d'authentification invalide");
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseAndValidateClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        Claims claims = parseAndValidateClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public long getExpirationInSeconds() {
        return expiration / 1000;
    }
}
