package com.njangi.auth.service;

import com.njangi.auth.dto.*;
import com.njangi.auth.entity.StatutUtilisateur;
import com.njangi.auth.entity.Utilisateur;
import com.njangi.auth.event.UtilisateurEventPublisher;
import com.njangi.auth.exception.BusinessException;
import com.njangi.auth.exception.NotFoundException;
import com.njangi.auth.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UtilisateurRepository utilisateurRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurEventPublisher eventPublisher;

    public AuthService(
            UtilisateurRepository utilisateurRepository,
            OtpService otpService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            UtilisateurEventPublisher eventPublisher
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public LoginResponse inscrire(InscriptionRequest request) {
        String telephoneNormalise = normaliserTelephone(request.telephone());

        if (utilisateurRepository.existsByTelephone(telephoneNormalise)) {
            throw new BusinessException("Ce numéro de téléphone est déjà enregistré sur Njangi : " + telephoneNormalise);
        }

        if (request.email() != null && !request.email().isBlank() && utilisateurRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new BusinessException("Cette adresse email est déjà enregistrée : " + request.email());
        }

        String motDePasseHash = passwordEncoder.encode(request.motDePasse());

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setTelephone(telephoneNormalise);
        utilisateur.setEmail(request.email() != null && !request.email().isBlank() ? request.email().trim().toLowerCase() : null);
        utilisateur.setMotDePasseHash(motDePasseHash);
        utilisateur.setNom(request.nom().trim());
        utilisateur.setPrenom(request.prenom().trim());
        utilisateur.setPhotoUrl(request.photoUrl());
        utilisateur.setVille(request.ville() != null ? request.ville().trim() : null);
        utilisateur.setPays(request.pays() != null ? request.pays().trim() : "Cameroun");
        utilisateur.setStatut(StatutUtilisateur.ACTIF);

        Utilisateur saved = utilisateurRepository.save(utilisateur);
        log.info("Nouvel utilisateur inscrit avec succès : ID={}, Tél={}", saved.getId(), saved.getTelephone());

        eventPublisher.publishUtilisateurInscrit(saved.getId(), saved.getTelephone(), saved.getNom() + " " + saved.getPrenom());

        String token = jwtService.generateToken(saved);
        return LoginResponse.of(token, jwtService.getExpirationInSeconds(), toDto(saved));
    }

    @Transactional
    public LoginResponse connexion(LoginRequest request) {
        String identifiant = request.identifiant().trim();

        // Normaliser si c'est un numéro de téléphone
        if (identifiant.matches("^(\\+[1-9][0-9]{6,14}|[236][0-9]{8})$")) {
            identifiant = normaliserTelephone(identifiant);
        } else {
            identifiant = identifiant.toLowerCase();
        }

        final String lookupId = identifiant;
        Utilisateur utilisateur = utilisateurRepository.findByIdentifiant(lookupId)
                .orElseThrow(() -> new BusinessException("Identifiant ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.motDePasse(), utilisateur.getMotDePasseHash())) {
            throw new BusinessException("Identifiant ou mot de passe incorrect");
        }

        if (utilisateur.getStatut() == StatutUtilisateur.SUSPENDU) {
            throw new BusinessException("Votre compte a été suspendu. Veuillez contacter l'administrateur de votre tontine.");
        }

        log.info("Connexion par mot de passe réussie : ID={}, Identifiant={}", utilisateur.getId(), lookupId);
        eventPublisher.publishUtilisateurConnecte(utilisateur.getId(), lookupId);

        String token = jwtService.generateToken(utilisateur);
        return LoginResponse.of(token, jwtService.getExpirationInSeconds(), toDto(utilisateur));
    }

    public void demanderOtp(DemandeOtpRequest request) {
        String identifiant = request.identifiant().trim();
        if (identifiant.matches("^(\\+[1-9][0-9]{6,14}|[236][0-9]{8})$")) {
            identifiant = normaliserTelephone(identifiant);
        }
        otpService.genererEtEnvoyer(identifiant);
    }

    @Transactional
    public LoginResponse verifierOtp(OtpVerifyRequest request) {
        String identifiant = request.identifiant().trim();
        if (identifiant.matches("^(\\+[1-9][0-9]{6,14}|[236][0-9]{8})$")) {
            identifiant = normaliserTelephone(identifiant);
        }

        otpService.verifierOtp(identifiant, request.code());

        final String finalIdentifiant = identifiant;
        Utilisateur utilisateur = utilisateurRepository.findByIdentifiant(finalIdentifiant)
                .orElseGet(() -> {
                    // Création automatique lors d'une première connexion OTP
                    Utilisateur nouveau = new Utilisateur();
                    if (finalIdentifiant.contains("@")) {
                        nouveau.setEmail(finalIdentifiant);
                        nouveau.setTelephone("+237000000000"); // Temporaire si email
                    } else {
                        nouveau.setTelephone(finalIdentifiant);
                    }
                    nouveau.setNom("Membre");
                    nouveau.setPrenom("Njangi");
                    nouveau.setMotDePasseHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                    nouveau.setStatut(StatutUtilisateur.ACTIF);
                    return utilisateurRepository.save(nouveau);
                });

        if (utilisateur.getStatut() == StatutUtilisateur.EN_ATTENTE_VERIFICATION) {
            utilisateur.setStatut(StatutUtilisateur.ACTIF);
            utilisateur = utilisateurRepository.save(utilisateur);
        }

        if (utilisateur.getStatut() == StatutUtilisateur.SUSPENDU) {
            throw new BusinessException("Ce compte est suspendu.");
        }

        log.info("Connexion par OTP réussie : ID={}, Identifiant={}", utilisateur.getId(), finalIdentifiant);
        eventPublisher.publishUtilisateurConnecte(utilisateur.getId(), finalIdentifiant);

        String token = jwtService.generateToken(utilisateur);
        return LoginResponse.of(token, jwtService.getExpirationInSeconds(), toDto(utilisateur));
    }

    @Transactional(readOnly = true)
    public UtilisateurDto moi(UUID userId) {
        Utilisateur u = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable avec l'ID : " + userId));
        return toDto(u);
    }

    public TokenValidationResponse validerToken(String token) {
        String cleanToken = token != null && token.startsWith("Bearer ") ? token.substring(7) : token;
        if (cleanToken == null || !jwtService.isTokenValid(cleanToken)) {
            return TokenValidationResponse.invalid("Jeton expiré ou invalide");
        }

        try {
            UUID userId = jwtService.extractUserId(cleanToken);
            Utilisateur u = utilisateurRepository.findById(userId).orElse(null);
            if (u == null || u.getStatut() == StatutUtilisateur.SUSPENDU) {
                return TokenValidationResponse.invalid("Utilisateur inactif ou introuvable");
            }
            return TokenValidationResponse.valid(u.getId(), u.getTelephone(), u.getEmail(), u.getNom() + " " + u.getPrenom());
        } catch (Exception ex) {
            return TokenValidationResponse.invalid("Erreur de décodage du jeton : " + ex.getMessage());
        }
    }

    @Transactional
    public SocialLoginResponse connexionSociale(SocialLoginRequest request) {
        String provider = request.provider().trim().toUpperCase();
        if (!"GOOGLE".equals(provider) && !"FACEBOOK".equals(provider)) {
            throw new BusinessException("Fournisseur social non supporté : " + provider + ". Utilisez 'GOOGLE' ou 'FACEBOOK'.");
        }

        String providerId = request.providerId().trim();
        String email = request.email() != null && !request.email().isBlank() ? request.email().trim().toLowerCase() : null;

        // 1. Chercher par providerAuth + providerId
        Utilisateur utilisateur = utilisateurRepository.findByProviderAuthAndProviderId(provider, providerId)
                .orElse(null);

        // 2. Si non trouvé, chercher par email si renseigné
        if (utilisateur == null && email != null) {
            utilisateur = utilisateurRepository.findByEmail(email).orElse(null);
            if (utilisateur != null) {
                utilisateur.setProviderAuth(provider);
                utilisateur.setProviderId(providerId);
                if (request.photoUrl() != null && (utilisateur.getPhotoUrl() == null || utilisateur.getPhotoUrl().isBlank())) {
                    utilisateur.setPhotoUrl(request.photoUrl());
                }
                utilisateur = utilisateurRepository.save(utilisateur);
                log.info("Compte existant par email lié avec succès au profil social {} : ID={}", provider, utilisateur.getId());
            }
        }

        // 3. Si toujours non trouvé, création automatique (JIT provisioning)
        if (utilisateur == null) {
            utilisateur = new Utilisateur();
            utilisateur.setProviderAuth(provider);
            utilisateur.setProviderId(providerId);
            utilisateur.setEmail(email);
            utilisateur.setNom(request.nom() != null && !request.nom().isBlank() ? request.nom().trim() : "Membre");
            utilisateur.setPrenom(request.prenom() != null && !request.prenom().isBlank() ? request.prenom().trim() : provider);
            utilisateur.setPhotoUrl(request.photoUrl());
            utilisateur.setStatut(StatutUtilisateur.ACTIF);
            utilisateur.setPays("Cameroun");
            utilisateur.setMotDePasseHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            utilisateur = utilisateurRepository.save(utilisateur);
            log.info("Nouveau compte social créé avec succès via {} : ID={}", provider, utilisateur.getId());
            eventPublisher.publishUtilisateurInscrit(utilisateur.getId(), email != null ? email : providerId, utilisateur.getNom() + " " + utilisateur.getPrenom());
        }

        if (utilisateur.getStatut() == StatutUtilisateur.SUSPENDU) {
            throw new BusinessException("Ce compte est suspendu. Veuillez contacter l'administrateur de votre tontine.");
        }

        eventPublisher.publishUtilisateurConnecte(utilisateur.getId(), provider + ":" + providerId);

        boolean telephoneRequis = (utilisateur.getTelephone() == null || utilisateur.getTelephone().isBlank());
        String token = jwtService.generateToken(utilisateur);

        return SocialLoginResponse.of(token, jwtService.getExpirationInSeconds(), toDto(utilisateur), telephoneRequis);
    }

    @Transactional
    public LoginResponse lierTelephone(UUID userId, LierTelephoneRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable avec l'ID : " + userId));

        String telephoneNormalise = normaliserTelephone(request.telephone());

        // Vérifier si le téléphone est déjà utilisé par un AUTRE utilisateur
        utilisateurRepository.findByTelephone(telephoneNormalise).ifPresent(autre -> {
            if (!autre.getId().equals(userId)) {
                throw new BusinessException("Ce numéro de téléphone est déjà associé à un autre compte Njangi : " + telephoneNormalise);
            }
        });

        // Si un code OTP est fourni, le vérifier
        if (request.codeOtp() != null && !request.codeOtp().isBlank()) {
            otpService.verifierOtp(telephoneNormalise, request.codeOtp());
        } else {
            // Si pas de code, générer un code OTP et demander de le valider
            otpService.genererEtEnvoyer(telephoneNormalise);
            throw new BusinessException("Un code OTP de confirmation a été envoyé au " + telephoneNormalise + ". Veuillez fournir le code pour finaliser la liaison.");
        }

        utilisateur.setTelephone(telephoneNormalise);
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        log.info("Numéro de téléphone {} lié avec succès au compte social ID={}", telephoneNormalise, userId);

        String token = jwtService.generateToken(saved);
        return LoginResponse.of(token, jwtService.getExpirationInSeconds(), toDto(saved));
    }

    private String normaliserTelephone(String telephone) {
        String clean = telephone.trim().replaceAll("\\s+", "");
        if (clean.matches("^[236][0-9]{8}$")) {
            return "+237" + clean;
        }
        return clean;
    }

    public UtilisateurDto toDto(Utilisateur u) {
        return new UtilisateurDto(
                u.getId(),
                u.getTelephone(),
                u.getEmail(),
                u.getNom(),
                u.getPrenom(),
                u.getPhotoUrl(),
                u.getVille(),
                u.getPays(),
                u.getStatut() != null ? u.getStatut().name() : StatutUtilisateur.ACTIF.name(),
                u.getCreeLe() != null ? u.getCreeLe() : LocalDateTime.now()
        );
    }
}
