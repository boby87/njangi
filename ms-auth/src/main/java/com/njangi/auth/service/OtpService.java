package com.njangi.auth.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final Map<String, String> otpStore = new ConcurrentHashMap<>();

    public void genererEtEnvoyer(String identifiant) {
        String code = String.format("%06d", (int)(Math.random() * 1_000_000));
        otpStore.put(identifiant, code);
        // TODO: envoyer via ms-notifications (Kafka event)
    }

    public void verifier(String identifiant, String code) {
        String stored = otpStore.get(identifiant);
        if (stored == null || !stored.equals(code)) {
            throw new IllegalArgumentException("Code OTP invalide ou expiré");
        }
        otpStore.remove(identifiant);
    }
}
