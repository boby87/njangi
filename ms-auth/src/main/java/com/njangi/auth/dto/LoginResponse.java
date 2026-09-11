package com.njangi.auth.dto;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    UtilisateurDto utilisateur
) {}
