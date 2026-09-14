package com.njangi.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean envoyerEmail(String destinataireEmail, String sujet, String contenu) {
        if (mailSender == null) {
            log.info("[EMAIL-SIMULATION] À: {} | Sujet: {} | Contenu: {}", destinataireEmail, sujet, contenu);
            return true;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataireEmail);
            message.setSubject(sujet);
            message.setText(contenu);
            mailSender.send(message);
            log.info("Email envoyé avec succès à : {}", destinataireEmail);
            return true;
        } catch (Exception e) {
            log.warn("Impossible d'envoyer l'email à {} ({}) — enregistrement en fallback", destinataireEmail, e.getMessage());
            return false;
        }
    }
}
