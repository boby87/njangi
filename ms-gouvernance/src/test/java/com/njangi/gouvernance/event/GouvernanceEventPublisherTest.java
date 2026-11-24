package com.njangi.gouvernance.event;

import com.njangi.gouvernance.config.KafkaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GouvernanceEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private GouvernanceEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new GouvernanceEventPublisher(kafkaTemplate);
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("Publication d'un événement GROUPE_CREE sur le topic unifié gouvernance.events")
    void testPublierGroupeCree() {
        UUID groupeId = UUID.randomUUID();
        UUID createurId = UUID.randomUUID();

        publisher.publierGroupeCree(groupeId, "Njangi Bafoussam", createurId, "ROTATIF");

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(kafkaTemplate).send(eq(KafkaConfig.TOPIC_GOUVERNANCE_EVENTS), eq(groupeId.toString()), captor.capture());

        DomainEvent event = captor.getValue();
        assertNotNull(event.eventId());
        assertEquals("GROUPE_CREE", event.eventType());
        assertEquals("ms-gouvernance", event.sourceService());
        assertEquals(groupeId, event.groupeId());
        assertEquals(createurId, event.acteurId());
        assertNotNull(event.occurredAt());
        assertEquals("Njangi Bafoussam", event.payload().get("nom"));
        assertEquals("ROTATIF", event.payload().get("typeSiege"));
    }

    @Test
    @DisplayName("Publication des événements unifiés BUREAU_ELU et REUNION_PLANIFIEE sur gouvernance.events")
    void testPublierBureauEluEtReunionPlanifiee() {
        UUID groupeId = UUID.randomUUID();
        UUID presidentId = UUID.randomUUID();
        UUID tresorierId = UUID.randomUUID();
        UUID secretaireId = UUID.randomUUID();
        UUID createurId = UUID.randomUUID();
        UUID reunionId = UUID.randomUUID();

        publisher.publierBureauElu(groupeId, presidentId, tresorierId, secretaireId, createurId);
        publisher.publierReunionPlanifiee(reunionId, groupeId, "Séance Inaugurale", LocalDateTime.of(2026, 5, 10, 14, 0));

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(kafkaTemplate, times(2)).send(eq(KafkaConfig.TOPIC_GOUVERNANCE_EVENTS), eq(groupeId.toString()), captor.capture());

        DomainEvent bureauEvent = captor.getAllValues().get(0);
        assertEquals("BUREAU_ELU", bureauEvent.eventType());
        assertEquals("ms-gouvernance", bureauEvent.sourceService());
        assertEquals(createurId.toString(), bureauEvent.payload().get("createurRetrogradeId"));

        DomainEvent reunionEvent = captor.getAllValues().get(1);
        assertEquals("REUNION_PLANIFIEE", reunionEvent.eventType());
        assertEquals("ms-gouvernance", reunionEvent.sourceService());
        assertEquals(reunionId.toString(), reunionEvent.payload().get("reunionId"));
    }
}
