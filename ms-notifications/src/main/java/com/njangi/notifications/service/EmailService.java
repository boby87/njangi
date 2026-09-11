package com.njangi.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void envoyerEmail(String destinataire, String sujet, String contenu) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject(sujet);
            message.setText(contenu);
            mailSender.send(message);
            log.info("Email envoyé à {} avec le sujet : {}", destinataire, sujet);
        } catch (Exception e) {
            log.error("Échec de l'envoi de l'email à {} : {}", destinataire, e.getMessage(), e);
            throw new RuntimeException("Échec de l'envoi de l'email : " + e.getMessage(), e);
        }
    }
}
