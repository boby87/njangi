package com.njangi.groupes.service;

import com.njangi.groupes.dto.CreerSessionRequest;
import com.njangi.groupes.dto.SessionTontineDto;
import com.njangi.groupes.entity.SessionTontine;
import com.njangi.groupes.entity.StatutSession;
import com.njangi.groupes.event.GroupeEventPublisher;
import com.njangi.groupes.exception.BusinessException;
import com.njangi.groupes.repository.GroupeRepository;
import com.njangi.groupes.repository.SessionTontineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionTontineServiceTest {

    @Mock
    private SessionTontineRepository sessionRepository;

    @Mock
    private GroupeRepository groupeRepository;

    @Mock
    private GroupeEventPublisher eventPublisher;

    private SessionTontineService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionTontineService(sessionRepository, groupeRepository, eventPublisher);
    }

    @Test
    @DisplayName("Planification d'une nouvelle session tontinière (Statut initial PLANIFIEE)")
    void testCreerSession() {
        UUID groupeId = UUID.randomUUID();
        when(groupeRepository.existsById(groupeId)).thenReturn(true);
        when(sessionRepository.save(any(SessionTontine.class))).thenAnswer(inv -> {
            SessionTontine s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        CreerSessionRequest req = new CreerSessionRequest(
                "Cycle 2026 - 2027",
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                BigDecimal.valueOf(500000),
                12
        );

        SessionTontineDto dto = sessionService.creerSession(groupeId, req);

        assertNotNull(dto);
        assertEquals("Cycle 2026 - 2027", dto.libelle());
        assertEquals(StatutSession.PLANIFIEE, dto.statut());
        verify(eventPublisher, times(1)).publierSessionCreee(eq(groupeId), any(), eq("Cycle 2026 - 2027"));
    }

    @Test
    @DisplayName("Démarrage d'une session tontinière (Statut passe à EN_COURS)")
    void testDemarrerSession() {
        UUID sessionId = UUID.randomUUID();
        UUID groupeId = UUID.randomUUID();
        SessionTontine session = new SessionTontine();
        session.setId(sessionId);
        session.setGroupeId(groupeId);
        session.setStatut(StatutSession.PLANIFIEE);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.findByGroupeIdAndStatut(groupeId, StatutSession.EN_COURS)).thenReturn(Optional.empty());
        when(sessionRepository.save(any(SessionTontine.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionTontineDto dto = sessionService.demarrerSession(sessionId);

        assertEquals(StatutSession.EN_COURS, dto.statut());
        verify(eventPublisher, times(1)).publierSessionDemarree(eq(groupeId), eq(sessionId));
    }

    @Test
    @DisplayName("Refus de démarrer une session si une autre est déjà en cours dans le groupe (Règle d'unicité)")
    void testDemarrerDeuxiemeSessionActive() {
        UUID sessionId = UUID.randomUUID();
        UUID groupeId = UUID.randomUUID();
        SessionTontine session = new SessionTontine();
        session.setId(sessionId);
        session.setGroupeId(groupeId);
        session.setStatut(StatutSession.PLANIFIEE);

        SessionTontine activeDeja = new SessionTontine();
        activeDeja.setId(UUID.randomUUID());
        activeDeja.setStatut(StatutSession.EN_COURS);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.findByGroupeIdAndStatut(groupeId, StatutSession.EN_COURS)).thenReturn(Optional.of(activeDeja));

        assertThrows(BusinessException.class, () -> sessionService.demarrerSession(sessionId));
    }

    @Test
    @DisplayName("Clôture d'une session tontinière (Statut passe à CLOTUREE)")
    void testCloturerSession() {
        UUID sessionId = UUID.randomUUID();
        UUID groupeId = UUID.randomUUID();
        SessionTontine session = new SessionTontine();
        session.setId(sessionId);
        session.setGroupeId(groupeId);
        session.setStatut(StatutSession.EN_COURS);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(SessionTontine.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionTontineDto dto = sessionService.cloturerSession(sessionId);

        assertEquals(StatutSession.CLOTUREE, dto.statut());
        verify(eventPublisher, times(1)).publierSessionCloturee(eq(groupeId), eq(sessionId));
    }
}
