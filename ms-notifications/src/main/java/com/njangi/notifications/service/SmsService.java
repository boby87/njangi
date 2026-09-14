package com.njangi.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    /**
     * Envoi d'un SMS au format international E.164 (+237... ou diaspora).
     * En production, intégration via passerelle SMS Orange / MTN MoMo ou Twilio / Infobip.
     * En environnement local / test, journalise l'envoi avec accusé de réception simulé.
     */
    public boolean envoyerSms(String telephone, String message) {
        if (telephone == null || telephone.isBlank()) {
            log.warn("[SMS-ECHEC] Numéro de téléphone destinataire manquant pour le message: {}", message);
            return false;
        }

        String formattedPhone = formatNumero(telephone);
        log.info("[SMS-ENVOI] Destinataire: {} | Longueur: {} chars | Contenu: {}",
                formattedPhone, message.length(), message);

        // Simulation de livraison réussie
        return true;
    }

    private String formatNumero(String raw) {
        String cleaned = raw.replaceAll("\\s+", "");
        if (cleaned.startsWith("+")) {
            return cleaned;
        }
        if (cleaned.startsWith("237")) {
            return "+" + cleaned;
        }
        if (cleaned.length() == 9 && (cleaned.startsWith("6") || cleaned.startsWith("2"))) {
            return "+237" + cleaned;
        }
        return cleaned;
    }
}
