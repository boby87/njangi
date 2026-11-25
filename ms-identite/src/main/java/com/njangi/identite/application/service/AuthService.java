package com.njangi.identite.application.service;

import com.njangi.identite.application.dto.*;
import com.njangi.identite.domain.exception.BusinessException;
import com.njangi.identite.domain.exception.NotFoundException;
import com.njangi.identite.domain.model.SocialUserProfile;
import com.njangi.identite.domain.model.StatutUtilisateur;
import com.njangi.identite.domain.model.Telephone;
import com.njangi.identite.domain.model.Utilisateur;
import com.njangi.identite.domain.port.out.IdentiteEventPublisherPort;
import com.njangi.identite.domain.port.out.SocialTokenVerifierPort;
import com.njangi.identite.domain.port.out.UtilisateurRepositoryPort;
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

    private final UtilisateurRepositoryPort utilisateurRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final IdentiteEventPublisherPort eventPublisher;
    private final SocialTokenVerifierPort socialTokenVerifier;

    public AuthService(
            UtilisateurRepositoryPort utilisateurRepository,
            OtpService otpService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            IdentiteEventPublisherPort eventPublisher,
            SocialTokenVerifierPort socialTokenVerifier
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.socialTokenVerifier = socialTokenVerifier;
    }

    @Transactional
    public LoginResponse inscrire(InscriptionRequest request) {
        String telephoneNormalise = new Telephone(request.telephone()).numero();

        if (utilisateurRepository.existeParTelephone(telephoneNormalise)) {
            throw new BusinessException("Ce numéro de téléphone est déjà enregistré sur Njangi : " + telephoneNormalise);
        }

        String emailNormalise = (request.email() != null && !request.email().isBlank())
                ? request.email().trim().toLowerCase()
                : null;
        if (emailNormalise != null && utilisateurRepository.existeParEmail(emailNormalise)) {
            throw new BusinessException("Cette adresse email est déjà enregistrée : " + request.email());
        }

        String motDePasseHash = passwordEncoder.encode(request.motDePasse());

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setTelephone(telephoneNormalise);
        utilisateur.setEmail(emailNormalise);
        utilisateur.setMotDePasseHash(motDePasseHash);
        utilisateur.setNom(request.nom().trim());
        utilisateur.setPrenom(request.prenom().trim());
        utilisateur.setPhotoUrl(request.photoUrl());
        utilisateur.setVille(request.ville() != null ? request.ville().trim() : null);
        utilisateur.setPays(request.pays() != null ? request.pays().trim() : "Cameroun");
        utilisateur.setStatut(StatutUtilisateur.ACTIF);

        Utilisateur saved = utilisateurRepository.sauvegarder(utilisateur);
        log.info("Nouvel utilisateur inscrit avec succès : ID={}, Tél={}", saved.getId(), saved.getTelephone());

        eventPublisher.publishUtilisateurInscrit(saved.getId(), saved.getTelephone(), saved.getNomComplet());

        String token = jwtService.generateToken(saved);
        return LoginResponse.of(token, jwtService.getExpirationInSeconds(), toDto(saved));
    }

    @Transactional
    public LoginResponse connexion(LoginRequest request) {
        String identifiant = normaliserIdentifiant(request.identifiant());

        Utilisateur utilisateur = utilisateurRepository.trouverParIdentifiant(identifiant)
                .orElseThrow(() -> new BusinessException("Identifiant ou mot de passe incorrect"));

        if (utilisateur.getMotDePasseHash() == null
                || !passwordEncoder.matches(request.motDePasse(), utilisateur.getMotDePasseHash())) {
            throw new BusinessException("Identifiant ou mot de passe incorrect");
        }

        if (utilisateur.isSuspendu()) {
            throw new BusinessException("Votre compte a été suspendu. Veuillez contacter l'administrateur de votre tontine.");
        }

        log.info("Connexion par mot de passe réussie : ID={}, Identifiant={}", utilisateur.getId(), identifiant);
        eventPublisher.publishUtilisateurConnecte(utilisateur.getId(), identifiant);

        String token = jwtService.generateToken(utilisateur);
        return LoginResponse.of(token, jwtService.getExpirationInSeconds(), toDto(utilisateur));
    }

    public void demanderOtp(DemandeOtpRequest request) {
        String identifiant = normaliserIdentifiant(request.identifiant());
        otpService.genererEtEnvoyer(identifiant);
    }

    @Transactional
    public LoginResponse verifierOtp(OtpVerifyRequest request) {
        String identifiant = normaliserIdentifiant(request.identifiant());

        otpService.verifierOtp(identifiant, request.code());

        final String finalIdentifiant = identifiant;
        Utilisateur utilisateur = utilisateurRepository.trouverParIdentifiant(finalIdentifiant)
                .orElseGet(() -> {
                    Utilisateur nouveau = new Utilisateur();
                    if (finalIdentifiant.contains("@")) {
                        nouveau.setEmail(finalIdentifiant);
                    } else {
                        nouveau.setTelephone(finalIdentifiant);
                    }
                    nouveau.setNom("Membre");
                    nouveau.setPrenom("Njangi");
                    nouveau.setPays("Cameroun");
                    nouveau.setMotDePasseHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                    nouveau.setStatut(StatutUtilisateur.ACTIF);
                    return utilisateurRepository.sauvegarder(nouveau);
                });

        if (utilisateur.getStatut() == StatutUtilisateur.EN_ATTENTE_VERIFICATION) {
            utilisateur.setStatut(StatutUtilisateur.ACTIF);
            utilisateur = utilisateurRepository.sauvegarder(utilisateur);
        }

        if (utilisateur.isSuspendu()) {
            throw new BusinessException("Ce compte est suspendu.");
        }

        log.info("Connexion par OTP réussie : ID={}, Identifiant={}", utilisateur.getId(), finalIdentifiant);
        eventPublisher.publishUtilisateurConnecte(utilisateur.getId(), finalIdentifiant);

        String token = jwtService.generateToken(utilisateur);
        return LoginResponse.of(token, jwtService.getExpirationInSeconds(), toDto(utilisateur));
    }

    @Transactional(readOnly = true)
    public UtilisateurDto moi(UUID userId) {
        Utilisateur u = utilisateurRepository.trouverParId(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable avec l'ID : " + userId));
        return toDto(u);
    }

    @Transactional(readOnly = true)
    public TokenValidationResponse validerToken(String token) {
        String cleanToken = token != null && token.startsWith("Bearer ") ? token.substring(7) : token;
        if (cleanToken == null || !jwtService.isTokenValid(cleanToken)) {
            return TokenValidationResponse.invalid("Jeton expiré ou invalide");
        }

        try {
            UUID userId = jwtService.extractUserId(cleanToken);
            Utilisateur u = utilisateurRepository.trouverParId(userId).orElse(null);
            if (u == null || u.isSuspendu()) {
                return TokenValidationResponse.invalid("Utilisateur inactif ou introuvable");
            }
            return TokenValidationResponse.valid(u.getId(), u.getTelephone(), u.getEmail(), u.getNomComplet());
        } catch (Exception ex) {
            return TokenValidationResponse.invalid("Erreur de décodage du jeton : " + ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public SsoPublicConfigDto getSsoPublicConfig() {
        return new SsoPublicConfigDto(
                socialTokenVerifier.isGoogleConfigured(),
                socialTokenVerifier.getPrimaryGoogleClientId(),
                socialTokenVerifier.isDevFallbackEnabled()
        );
    }

    @Transactional
    public SocialLoginResponse connexionSociale(SocialLoginRequest request) {
        // 0. Vérification cryptographique du jeton auprès du fournisseur SSO (Google OIDC JWKS)
        SocialUserProfile verified = socialTokenVerifier.verifierEtExtraire(
                request.provider(),
                request.token(),
                request.providerId(),
                request.email(),
                request.nom(),
                request.prenom(),
                request.photoUrl()
        );

        String provider = verified.provider();
        String providerId = verified.providerId();
        String email = verified.email();

        // 1. Chercher par providerAuth + providerId (identifiant immuable 'sub')
        Utilisateur utilisateur = utilisateurRepository.trouverParProviderAuthEtProviderId(provider, providerId)
                .orElse(null);

        // 2. Si non trouvé, lier au compte existant par email UNIQUEMENT si l'email est vérifié par le fournisseur (anti account-takeover)
        if (utilisateur == null && email != null && verified.emailVerified()) {
            utilisateur = utilisateurRepository.trouverParEmail(email).orElse(null);
            if (utilisateur != null) {
                utilisateur.setProviderAuth(provider);
                utilisateur.setProviderId(providerId);
                if (verified.photoUrl() != null && (utilisateur.getPhotoUrl() == null || utilisateur.getPhotoUrl().isBlank())) {
                    utilisateur.setPhotoUrl(verified.photoUrl());
                }
                utilisateur = utilisateurRepository.sauvegarder(utilisateur);
                log.info("Compte existant par email vérifié lié avec succès au profil SSO {} : ID={}", provider, utilisateur.getId());
            }
        }

        // 3. Si toujours non trouvé, création automatique (JIT provisioning) depuis les claims certifiés
        if (utilisateur == null) {
            utilisateur = new Utilisateur();
            utilisateur.setProviderAuth(provider);
            utilisateur.setProviderId(providerId);
            utilisateur.setEmail(email);
            utilisateur.setNom(verified.nom());
            utilisateur.setPrenom(verified.prenom());
            utilisateur.setPhotoUrl(verified.photoUrl());
            utilisateur.setStatut(StatutUtilisateur.ACTIF);
            utilisateur.setPays("Cameroun");
            utilisateur.setMotDePasseHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            utilisateur = utilisateurRepository.sauvegarder(utilisateur);
            log.info("Nouveau compte SSO créé avec succès via {} : ID={}", provider, utilisateur.getId());
            eventPublisher.publishUtilisateurInscrit(
                    utilisateur.getId(),
                    email != null ? email : providerId,
                    utilisateur.getNomComplet()
            );
        }

        if (utilisateur.isSuspendu()) {
            throw new BusinessException("Ce compte est suspendu. Veuillez contacter l'administrateur de votre tontine.");
        }

        eventPublisher.publishUtilisateurConnecte(utilisateur.getId(), provider + ":" + providerId);

        boolean telephoneRequis = utilisateur.needsTelephone();
        String token = jwtService.generateToken(utilisateur);

        return SocialLoginResponse.of(token, jwtService.getExpirationInSeconds(), toDto(utilisateur), telephoneRequis);
    }

    @Transactional
    public LoginResponse lierTelephone(UUID userId, LierTelephoneRequest request) {
        Utilisateur utilisateur = utilisateurRepository.trouverParId(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable avec l'ID : " + userId));

        String telephoneNormalise = new Telephone(request.telephone()).numero();

        utilisateurRepository.trouverParTelephone(telephoneNormalise).ifPresent(autre -> {
            if (!autre.getId().equals(userId)) {
                throw new BusinessException("Ce numéro de téléphone est déjà associé à un autre compte Njangi : " + telephoneNormalise);
            }
        });

        if (request.codeOtp() != null && !request.codeOtp().isBlank()) {
            otpService.verifierOtp(telephoneNormalise, request.codeOtp());
        } else {
            otpService.genererEtEnvoyer(telephoneNormalise);
            throw new BusinessException(
                    "Un code OTP de confirmation a été envoyé au " + telephoneNormalise + ". Veuillez fournir le code pour finaliser la liaison."
            );
        }

        utilisateur.setTelephone(telephoneNormalise);
        Utilisateur saved = utilisateurRepository.sauvegarder(utilisateur);
        log.info("Numéro de téléphone {} lié avec succès au compte social ID={}", telephoneNormalise, userId);

        String token = jwtService.generateToken(saved);
        return LoginResponse.of(token, jwtService.getExpirationInSeconds(), toDto(saved));
    }

    private String normaliserIdentifiant(String identifiant) {
        String trimmed = identifiant.trim();
        if (Telephone.isTelephonePattern(trimmed)) {
            return new Telephone(trimmed).numero();
        }
        return trimmed.toLowerCase();
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
