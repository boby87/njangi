package com.njangi.auth.service;

import com.njangi.auth.entity.Utilisateur;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${njangi.jwt.secret}")
    private String secret;

    @Value("${njangi.jwt.expiration}")
    private long expiration;

    public String generateToken(Utilisateur utilisateur) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .subject(utilisateur.getId().toString())
                .claim("email", utilisateur.getEmail())
                .claim("nom", utilisateur.getNom())
                .claim("prenom", utilisateur.getPrenom())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }
}
