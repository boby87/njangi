package com.njangi.notifications.event;

import com.njangi.notifications.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventPublisher.class);
    private static final String TOPIC_NOTIFICATION_ENVOYEE = "notification.envoyee";
    private static final String TOPIC_NOTIFICATION_DEMANDEE = "notification.demandee";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishNotificationEnvoyee(Notification notification) {
        Map<String, Object> event = buildPayload(notification, "NOTIFICATION_ENVOYEE");
        String key = notification.getDestinataireId().toString();
        kafkaTemplate.send(TOPIC_NOTIFICATION_ENVOYEE, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Erreur lors de la publication sur {} : {}", TOPIC_NOTIFICATION_ENVOYEE, ex.getMessage());
                    } else {
                        log.info("Événement {} publié pour notificationId={}", TOPIC_NOTIFICATION_ENVOYEE, notification.getId());
                    }
                });
    }

    public void publishNotificationDemandee(Notification notification) {
        Map<String, Object> event = buildPayload(notification, "NOTIFICATION_DEMANDEE");
        String key = notification.getDestinataireId().toString();
        kafkaTemplate.send(TOPIC_NOTIFICATION_DEMANDEE, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Erreur lors de la publication sur {} : {}", TOPIC_NOTIFICATION_DEMANDEE, ex.getMessage());
                    } else {
                        log.info("Événement {} publié pour notificationId={}", TOPIC_NOTIFICATION_DEMANDEE, notification.getId());
                    }
                });
    }

    private Map<String, Object> buildPayload(Notification n, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("notificationId", n.getId() != null ? n.getId().toString() : null);
        event.put("destinataireId", n.getDestinataireId().toString());
        event.put("groupeId", n.getGroupeId() != null ? n.getGroupeId().toString() : null);
        event.put("canal", n.getCanal().name());
        event.put("type", n.getType());
        event.put("titre", n.getTitre());
        event.put("contenu", n.getContenu());
        event.put("statut", n.getStatut().name());
        return event;
    }
}
