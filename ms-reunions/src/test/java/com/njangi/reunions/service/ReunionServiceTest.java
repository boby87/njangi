package com.njangi.reunions.service;

import com.njangi.reunions.dto.CloturerReunionRequest;
import com.njangi.reunions.dto.CreateReunionRequest;
import com.njangi.reunions.dto.ReunionDto;
import com.njangi.reunions.entity.Reunion;
import com.njangi.reunions.entity.StatutReunion;
import com.njangi.reunions.entity.TypeSiege;
import com.njangi.reunions.event.ReunionEventPublisher;
import com.njangi.reunions.exception.BusinessException;
import com.njangi.reunions.exception.NotFoundException;
import com.njangi.reunions.repository.ReunionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReunionServiceTest {

    @Mock
    private ReunionRepository reunionRepository;

    @Mock
    private ReunionEventPublisher eventPublisher;

    private ReunionService reunionService;

    @BeforeEach
    void setUp() {
        reunionService = new ReunionService(reunionRepository, eventPublisher);
    }

    @Test
    @DisplayName("Planification d'une réunion avec succès (Statut initial PLANIFIEE)")
    void testCreateReunion() {
        UUID groupeId = UUID.randomUUID();
        CreateReunionRequest request = new CreateReunionRequest(
                groupeId,
                UUID.randomUUID(),
                "Séance Mensuelle Akwa",
                LocalDateTime.now().plusDays(7),
                "Salle des Fêtes Akwa, Douala",
                TypeSiege.FIXE,
                null,
                "1. Appel nominatif 2. Cotisation pot rotatif 3. Divers",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        when(reunionRepository.save(any(Reunion.class))).thenAnswer(invocation -> {
            Reunion r = invocation.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        ReunionDto dto = reunionService.create(request);

        assertNotNull(dto);
        assertNotNull(dto.id());
        assertEquals("Séance Mensuelle Akwa", dto.titre());
        assertEquals(StatutReunion.PLANIFIEE, dto.statut());
        verify(reunionRepository, times(1)).save(any(Reunion.class));
        verify(eventPublisher, times(1)).publierReunionCreee(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Démarrer une réunion en direct (Statut passe à EN_COURS)")
    void testDemarrerReunion() {
        UUID id = UUID.randomUUID();
        Reunion reunion = new Reunion();
        reunion.setId(id);
        reunion.setGroupeId(UUID.randomUUID());
        reunion.setTitre("Séance Live");
        reunion.setStatut(StatutReunion.PLANIFIEE);

        when(reunionRepository.findById(id)).thenReturn(Optional.of(reunion));
        when(reunionRepository.save(any(Reunion.class))).thenAnswer(inv -> inv.getArgument(0));

        ReunionDto dto = reunionService.demarrerReunion(id);

        assertEquals(StatutReunion.EN_COURS, dto.statut());
        verify(eventPublisher, times(1)).publierReunionDemarree(eq(id), any(), eq("Séance Live"));
    }

    @Test
    @DisplayName("Clôturer une réunion avec compte-rendu officiel (Statut passe à TERMINEE)")
    void testTerminerReunion() {
        UUID id = UUID.randomUUID();
        Reunion reunion = new Reunion();
        reunion.setId(id);
        reunion.setGroupeId(UUID.randomUUID());
        reunion.setTitre("Séance de Clôture");
        reunion.setStatut(StatutReunion.EN_COURS);

        when(reunionRepository.findById(id)).thenReturn(Optional.of(reunion));
        when(reunionRepository.save(any(Reunion.class))).thenAnswer(inv -> inv.getArgument(0));

        CloturerReunionRequest request = new CloturerReunionRequest("Procès-verbal officiel : Pot attribué à M. Kamga (350 000 XAF).");
        ReunionDto dto = reunionService.terminerReunion(id, request);

        assertEquals(StatutReunion.TERMINEE, dto.statut());
        assertEquals("Procès-verbal officiel : Pot attribué à M. Kamga (350 000 XAF).", dto.compteRendu());
        verify(eventPublisher, times(1)).publierReunionTerminee(eq(id), any(), eq("Séance de Clôture"), anyString());
    }

    @Test
    @DisplayName("Refus de clôturer une réunion déjà terminée (BusinessException)")
    void testTerminerReunionDejaTerminee() {
        UUID id = UUID.randomUUID();
        Reunion reunion = new Reunion();
        reunion.setId(id);
        reunion.setStatut(StatutReunion.TERMINEE);

        when(reunionRepository.findById(id)).thenReturn(Optional.of(reunion));

        assertThrows(BusinessException.class, () -> reunionService.terminerReunion(id, new CloturerReunionRequest("PV")));
    }

    @Test
    @DisplayName("Annuler une réunion planifiée")
    void testAnnulerReunion() {
        UUID id = UUID.randomUUID();
        Reunion reunion = new Reunion();
        reunion.setId(id);
        reunion.setStatut(StatutReunion.PLANIFIEE);

        when(reunionRepository.findById(id)).thenReturn(Optional.of(reunion));
        when(reunionRepository.save(any(Reunion.class))).thenAnswer(inv -> inv.getArgument(0));

        ReunionDto dto = reunionService.annulerReunion(id);

        assertEquals(StatutReunion.ANNULEE, dto.statut());
    }

    @Test
    @DisplayName("Recherche par ID inexistant lève NotFoundException")
    void testFindByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(reunionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reunionService.findById(id));
    }
}
