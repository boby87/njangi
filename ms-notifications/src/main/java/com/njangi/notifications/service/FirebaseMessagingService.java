package com.njangi.notifications.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

@Service
public class FirebaseMessagingService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseMessagingService.class);

    private final ResourceLoader resourceLoader;

    @Value("${firebase.credentials-file:firebase-service-account.json}")
    private String credentialsFile;

    @Value("${firebase.project-id:njangi-tontine}")
    private String projectId;

    private boolean initialized = false;

    public FirebaseMessagingService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + credentialsFile);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(is))
                            .setProjectId(projectId)
                            .build();

                    if (FirebaseApp.getApps().isEmpty()) {
                        FirebaseApp.initializeApp(options);
                    }
                    this.initialized = true;
                    log.info("Firebase Cloud Messaging initialisé avec succès pour le projet : {}", projectId);
                }
            } else {
                log.warn("Fichier d'identifiants Firebase non trouvé ({}) — mode simulation actif pour le Push FCM", credentialsFile);
            }
        } catch (Exception e) {
            log.warn("Initialisation FCM ignorée ({}) — mode simulation actif", e.getMessage());
        }
    }

    public boolean envoyerPush(String deviceToken, String titre, String corps, Map<String, String> data) {
        if (!initialized || deviceToken == null || deviceToken.isBlank()) {
            log.info("[FCM-SIMULATION] Token: {} | Titre: {} | Corps: {}", deviceToken, titre, corps);
            return true;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(titre)
                            .setBody(corps)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Notification FCM envoyée avec succès, id: {}", response);
            return true;
        } catch (Exception e) {
            log.error("Échec de l'envoi Push FCM vers token={} : {}", deviceToken, e.getMessage());
            return false;
        }
    }
}
