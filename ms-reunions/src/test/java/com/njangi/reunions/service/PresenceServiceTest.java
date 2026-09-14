package com.njangi.reunions.service;

import com.njangi.reunions.dto.BatchPresencesRequest;
import com.njangi.reunions.dto.EnregistrerPresenceRequest;
import com.njangi.reunions.dto.PresenceDto;
import com.njangi.reunions.dto.QuorumDto;
import com.njangi.reunions.entity.Presence;
import com.njangi.reunions.entity.StatutPresence;
import com.njangi.reunions.event.ReunionEventPublisher;
import com.njangi.reunions.exception.NotFoundException;
import com.njangi.reunions.repository.PresenceRepository;
import com.njangi.reunions.repository.ReunionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresenceServiceTest {

    @Mock
    private PresenceRepository presenceRepository;

    @Mock
    private ReunionRepository reunionRepository;

    @Mock
    private ReunionEventPublisher eventPublisher;

    private PresenceService presenceService;

    @BeforeEach
    void setUp() {
        presenceService = new PresenceService(presenceRepository, reunionRepository, eventPublisher);
    }

    @Test
    @DisplayName("Enregistrement de la présence d'un membre individuel (PRESENT)")
    void testEnregistrerPresence() {
        UUID reunionId = UUID.randomUUID();
        UUID membreId = UUID.randomUUID();

        when(reunionRepository.existsById(reunionId)).thenReturn(true);
        when(presenceRepository.findByReunionIdAndMembreId(reunionId, membreId)).thenReturn(Optional.empty());
        when(presenceRepository.save(any(Presence.class))).thenAnswer(inv -> {
            Presence p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        EnregistrerPresenceRequest request = new EnregistrerPresenceRequest(
                membreId,
                StatutPresence.PRESENT,
                LocalDateTime.now(),
                null,
                false,
                null
        );

        PresenceDto dto = presenceService.enregistrerPresence(reunionId, request);

        assertNotNull(dto);
        assertEquals(StatutPresence.PRESENT, dto.statut());
        assertEquals(membreId, dto.membreId());
        verify(presenceRepository, times(1)).save(any(Presence.class));
        verify(eventPublisher, times(1)).publierPresenceEnregistree(eq(reunionId), eq(membreId), eq("PRESENT"));
    }

    @Test
    @DisplayName("Émargement par lot (Appel nominatif de séance)")
    void testEnregistrerPresencesBatch() {
        UUID reunionId = UUID.randomUUID();
        UUID m1 = UUID.randomUUID();
        UUID m2 = UUID.randomUUID();

        when(reunionRepository.existsById(reunionId)).thenReturn(true);
        when(presenceRepository.findByReunionIdAndMembreId(any(), any())).thenReturn(Optional.empty());
        when(presenceRepository.save(any(Presence.class))).thenAnswer(inv -> {
            Presence p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        BatchPresencesRequest request = new BatchPresencesRequest(List.of(
                new EnregistrerPresenceRequest(m1, StatutPresence.PRESENT, LocalDateTime.now(), null, false, null),
                new EnregistrerPresenceRequest(m2, StatutPresence.RETARD, LocalDateTime.now(), "Embouteillage Akwa", false, null)
        ));

        List<PresenceDto> list = presenceService.enregistrerPresencesBatch(reunionId, request);

        assertEquals(2, list.size());
        verify(presenceRepository, times(2)).save(any(Presence.class));
    }

    @Test
    @DisplayName("Calcul du quorum et statistiques de présence")
    void testCalculerQuorum() {
        UUID reunionId = UUID.randomUUID();

        when(reunionRepository.existsById(reunionId)).thenReturn(true);
        when(presenceRepository.countByReunionId(reunionId)).thenReturn(10L);
        when(presenceRepository.countByReunionIdAndStatut(reunionId, StatutPresence.PRESENT)).thenReturn(6L);
        when(presenceRepository.countByReunionIdAndStatut(reunionId, StatutPresence.RETARD)).thenReturn(1L);
        when(presenceRepository.countByReunionIdAndStatut(reunionId, StatutPresence.EXCUSE)).thenReturn(1L);
        when(presenceRepository.countByReunionIdAndStatut(reunionId, StatutPresence.ABSENT)).thenReturn(2L);

        QuorumDto quorum = presenceService.calculerQuorum(reunionId);

        assertNotNull(quorum);
        assertEquals(10, quorum.totalEnregistres());
        assertEquals(6, quorum.presents());
        assertEquals(1, quorum.retards());
        assertEquals(70.0, quorum.tauxPresencePourcent());
        assertTrue(quorum.quorumAtteint());
    }

    @Test
    @DisplayName("Présence sur une réunion inexistante lève NotFoundException")
    void testReunionInexistante() {
        UUID reunionId = UUID.randomUUID();
        when(reunionRepository.existsById(reunionId)).thenReturn(false);

        EnregistrerPresenceRequest req = new EnregistrerPresenceRequest(
                UUID.randomUUID(),
                StatutPresence.PRESENT,
                null,
                null,
                false,
                null
        );

        assertThrows(NotFoundException.class, () -> presenceService.enregistrerPresence(reunionId, req));
    }
}
