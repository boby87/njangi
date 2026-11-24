package com.njangi.finances.event;

import com.njangi.finances.config.KafkaConfig;
import com.njangi.finances.paiement.entity.ModePaiement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FinancesEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private FinancesEventPublisher eventPublisher;

    @Test
    @DisplayName("Doit publier un DomainEvent conforme sur le topic unifié finances.events")
    void doitPublierDomainEventSurFinancesEvents() {
        UUID paiementId = UUID.randomUUID();
        UUID cotisationId = UUID.randomUUID();
        UUID membreId = UUID.randomUUID();
        UUID groupeId = UUID.randomUUID();

        eventPublisher.publierPaiementValide(
                paiementId, cotisationId, membreId, groupeId,
                BigDecimal.valueOf(50000), ModePaiement.MTN_MOMO, "MOMO-12345678"
        );

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(kafkaTemplate).send(eq(KafkaConfig.TOPIC_FINANCES_EVENTS), eq(paiementId.toString()), captor.capture());

        DomainEvent event = captor.getValue();
        assertThat(event.eventId()).isNotNull();
        assertThat(event.eventType()).isEqualTo(FinancesEventPublisher.EVENT_PAIEMENT_VALIDE);
        assertThat(event.sourceService()).isEqualTo("ms-finances");
        assertThat(event.groupeId()).isEqualTo(groupeId);
        assertThat(event.acteurId()).isEqualTo(membreId);
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.payload())
                .containsEntry("paiementId", paiementId.toString())
                .containsEntry("cotisationId", cotisationId.toString())
                .containsEntry("membreId", membreId.toString())
                .containsEntry("modePaiement", "MTN_MOMO")
                .containsEntry("reference", "MOMO-12345678");
        assertThat((BigDecimal) event.payload().get("montant")).isEqualByComparingTo(BigDecimal.valueOf(50000));
    }
}
