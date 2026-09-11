package com.njangi.auth.service;

import com.njangi.auth.dto.*;
import com.njangi.auth.entity.Utilisateur;
import com.njangi.auth.exception.BusinessException;
import com.njangi.auth.exception.NotFoundException;
import com.njangi.auth.repository.UtilisateurRepository;
import com.njangi.auth.event.UtilisateurEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UtilisateurDto findById(UUID id) {
        return utilisateurRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable : " + id));
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.motDePasse(), utilisateur.getMotDePasseHash())) {
            throw new BusinessException("Email ou mot de passe incorrect");
        }

        String token = jwtService.generateToken(utilisateur);
        log.info("Connexion reussie pour l'utilisateur : {}", utilisateur.getEmail());

        return new LoginResponse(token, "Bearer", 86400L, toDto(utilisateur));
    }

    @Transactional
    public void genererOtp(String telephone) {
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephone)
                .orElseThrow(() -> new NotFoundException("Telephone non trouve : " + telephone));

        String otp = String.format("%06d", new Random().nextInt(999999));
        utilisateur.setOtpCode(passwordEncoder.encode(otp));
        utilisateur.setOtpExpiration(LocalDateTime.now().plusMinutes(5));
        utilisateurRepository.save(utilisateur);

        eventPublisher.publishOtpDemande(utilisateur.getTelephone(), otp);
        log.info("OTP genere pour le telephone : {}", telephone);
    }

    @Transactional
    public boolean verifierOtp(OtpVerificationRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(request.telephone())
                .orElseThrow(() -> new NotFoundException("Telephone non trouve"));

        if (utilisateur.getOtpExpiration() == null || LocalDateTime.now().isAfter(utilisateur.getOtpExpiration())) {
            throw new BusinessException("OTP expire");
        }

        boolean valid = passwordEncoder.matches(request.otpCode(), utilisateur.getOtpCode());
        if (valid) {
            utilisateur.setOtpCode(null);
            utilisateur.setOtpExpiration(null);
            utilisateurRepository.save(utilisateur);
        }
        return valid;
    }

    private UtilisateurDto toDto(Utilisateur u) {
        return new UtilisateurDto(u.getId(), u.getEmail(), u.getTelephone(),
                u.getNom(), u.getPrenom(), u.getStatut(), u.getCreatedAt());
    }
}
