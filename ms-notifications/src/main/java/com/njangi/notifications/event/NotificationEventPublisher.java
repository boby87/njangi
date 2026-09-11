package com.njangi.notifications.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publier(String topic, String key, Map<String, Object> payload) {
        log.debug("Publication sur le topic {} : key={}", topic, key);
        kafkaTemplate.send(topic, key, payload);
    }
}
