package com.njangi.identite.application.service;

import com.njangi.identite.domain.event.MembreInscritEvent;
import com.njangi.identite.domain.exception.BusinessException;
import com.njangi.identite.domain.model.Membre;
import com.njangi.identite.domain.model.StatutMembre;
import com.njangi.identite.domain.port.out.IdentiteEventPublisherPort;
import com.njangi.identite.domain.port.out.MembreRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscrireMembreServiceTest {

    @Mock
    private MembreRepositoryPort membreRepositoryPort;
    @Mock
    private IdentiteEventPublisherPort eventPublisherPort;

    private InscrireMembreService service;

    @BeforeEach
    void setUp() {
        service = new InscrireMembreService(membreRepositoryPort, eventPublisherPort);
    }

    @Test
    @DisplayName("Inscrit un nouveau membre avec normalisation E.164 et publie MEMBRE_INSCRIT")
    void testInscrireMembreSucces() {
        UUID authId = UUID.randomUUID();
        when(membreRepositoryPort.existeParEmail("jp.fokou@njangi.cm")).thenReturn(false);
        when(membreRepositoryPort.existeParTelephone("+237699123456")).thenReturn(false);
        when(membreRepositoryPort.sauvegarder(any(Membre.class))).thenAnswer(inv -> inv.getArgument(0));

        Membre result = service.inscrire(
                authId,
                "FOKOU",
                "Jean-Paul",
                "JP.Fokou@Njangi.CM",
                "699123456",
                LocalDate.of(1990, 5, 15),
                "Quartier Bastos",
                "Yaoundé"
        );

        assertNotNull(result.getId());
        assertEquals(authId, result.getAuthUtilisateurId());
        assertEquals("jp.fokou@njangi.cm", result.getEmail().value());
        assertEquals("+237699123456", result.getTelephone().numero());
        assertEquals(StatutMembre.ACTIF, result.getStatut());

        ArgumentCaptor<MembreInscritEvent> captor = ArgumentCaptor.forClass(MembreInscritEvent.class);
        verify(eventPublisherPort).publishMembreInscrit(captor.capture());
        assertEquals(result.getId(), captor.getValue().membreId());
        assertEquals("+237699123456", captor.getValue().telephone());
    }

    @Test
    @DisplayName("Refuse l'inscription si l'adresse email est déjà utilisée")
    void testInscrireEmailDoublon() {
        when(membreRepositoryPort.existeParEmail("existant@njangi.cm")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.inscrire(
                UUID.randomUUID(),
                "FOKOU",
                "Paul",
                "existant@njangi.cm",
                "+237699123456",
                null,
                null,
                "Douala"
        ));

        verify(membreRepositoryPort, never()).sauvegarder(any());
        verify(eventPublisherPort, never()).publishMembreInscrit(any());
    }

    @Test
    @DisplayName("Refuse l'inscription si le numéro de téléphone est déjà utilisé")
    void testInscrireTelephoneDoublon() {
        when(membreRepositoryPort.existeParEmail("nouveau@njangi.cm")).thenReturn(false);
        when(membreRepositoryPort.existeParTelephone("+237699123456")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.inscrire(
                UUID.randomUUID(),
                "FOKOU",
                "Paul",
                "nouveau@njangi.cm",
                "699123456",
                null,
                null,
                "Douala"
        ));

        verify(membreRepositoryPort, never()).sauvegarder(any());
        verify(eventPublisherPort, never()).publishMembreInscrit(any());
    }

    @Test
    @DisplayName("Refuse l'inscription si le numéro de téléphone est invalide")
    void testInscrireTelephoneInvalide() {
        assertThrows(IllegalArgumentException.class, () -> service.inscrire(
                UUID.randomUUID(),
                "FOKOU",
                "Paul",
                "valide@njangi.cm",
                "0000",
                null,
                null,
                "Douala"
        ));

        verify(membreRepositoryPort, never()).sauvegarder(any());
    }
}
