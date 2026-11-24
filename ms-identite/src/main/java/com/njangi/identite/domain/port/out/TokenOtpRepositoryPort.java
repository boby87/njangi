package com.njangi.identite.domain.port.out;

import com.njangi.identite.domain.model.TokenOtp;

import java.util.Optional;

public interface TokenOtpRepositoryPort {
    TokenOtp sauvegarder(TokenOtp tokenOtp);
    Optional<TokenOtp> trouverDernierNonUtiliseParIdentifiant(String identifiant);
}
