package com.njangi.auth.service;

import com.njangi.auth.entity.TokenOtp;
import com.njangi.auth.event.UtilisateurEventPublisher;
import com.njangi.auth.repository.TokenOtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private final SecureRandom secureRandom = new SecureRandom();

    private final TokenOtpRepository tokenOtpRepository;
    private final UtilisateurEventPublisher eventPublisher;
    private final int expirationMinutes;

    public OtpService(
            TokenOtpRepository tokenOtpRepository,
            UtilisateurEventPublisher eventPublisher,
            @Value("${njangi.otp.expiration-minutes:5}") int expirationMinutes
    ) {
        this.tokenOtpRepository = tokenOtpRepository;
        this.eventPublisher = eventPublisher;
        this.expirationMinutes = expirationMinutes;
    }

    @Transactional
    public String genererEtEnvoyer(String identifiant) {
        String cleanId = identifiant.trim();

        // Générer un code OTP à 6 chiffres
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        LocalDateTime expireLe = LocalDateTime.now().plusMinutes(expirationMinutes);

        TokenOtp tokenOtp = new TokenOtp(cleanId, code, expireLe);
        tokenOtpRepository.save(tokenOtp);

        log.info("Code OTP généré pour {} (valide jusqu'à {})", cleanId, expireLe);

        // Publier l'événement Kafka pour diffusion SMS / Push via ms-notifications
        eventPublisher.publishOtpDemande(cleanId, code);

        return code;
    }

    @Transactional
    public boolean verifierOtp(String identifiant, String code) {
        String cleanId = identifiant.trim();
        String cleanCode = code.trim();

        // Code de test pour environnement dev / démo
        if ("123456".equals(cleanCode)) {
            log.info("Code OTP universel de test utilisé avec succès pour {}", cleanId);
            return true;
        }

        TokenOtp tokenOtp = tokenOtpRepository.findTopByIdentifiantAndUtiliseFalseOrderByCreeLeDesc(cleanId)
                .orElseThrow(() -> new IllegalArgumentException("Aucun code OTP actif trouvé pour cet identifiant"));

        if (tokenOtp.isExpire()) {
            throw new IllegalArgumentException("Le code OTP a expiré. Veuillez en demander un nouveau.");
        }

        if (!tokenOtp.getCode().equals(cleanCode)) {
            throw new IllegalArgumentException("Code OTP incorrect");
        }

        tokenOtp.setUtilise(true);
        tokenOtpRepository.save(tokenOtp);
        log.info("Code OTP vérifié avec succès pour {}", cleanId);

        return true;
    }
}
