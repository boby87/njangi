package com.njangi.notifications.kafka;

import com.njangi.notifications.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {

    private static final String TOPIC_NOTIFICATION_DEMANDEE = "notification.demandee";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishNotificationDemandee(Notification notification) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "NOTIFICATION_DEMANDEE");
        event.put("notificationId", notification.getId().toString());
        event.put("destinatreId", notification.getDestinatreId().toString());
        event.put("groupeId", notification.getGroupeId() != null ? notification.getGroupeId().toString() : null);
        event.put("canal", notification.getCanal().name());
        event.put("type", notification.getType());
        event.put("titre", notification.getTitre());
        event.put("contenu", notification.getContenu());

        String key = notification.getDestinatreId().toString();
        kafkaTemplate.send(TOPIC_NOTIFICATION_DEMANDEE, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Erreur lors de la publication de {} pour notificationId={}",
                                TOPIC_NOTIFICATION_DEMANDEE, notification.getId(), ex);
                    } else {
                        log.info("Événement {} publié — notificationId={} canal={}",
                                TOPIC_NOTIFICATION_DEMANDEE, notification.getId(), notification.getCanal());
                    }
                });
    }
}
