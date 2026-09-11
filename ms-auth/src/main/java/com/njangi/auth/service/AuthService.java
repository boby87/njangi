package com.njangi.auth.service;

import com.njangi.auth.dto.*;
import com.njangi.auth.entity.Utilisateur;
import com.njangi.auth.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final OtpService otpService;

    public void demanderOtp(String identifiant) {
        otpService.genererEtEnvoyer(identifiant);
    }

    public String verifierOtp(OtpVerifyRequest request) {
        otpService.verifier(request.identifiant(), request.code());
        return "JWT_TOKEN_PLACEHOLDER";
    }

    public UtilisateurDto moi(UUID id) {
        Utilisateur u = utilisateurRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return new UtilisateurDto(u.getId(), u.getTelephone(), u.getEmail(),
            u.getNom(), u.getPrenom(), u.getPhotoUrl(), u.getVille(),
            u.getPays(), u.getStatut().name(), u.getCreeLe());
    }
}
