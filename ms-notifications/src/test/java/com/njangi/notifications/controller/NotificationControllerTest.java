package com.njangi.notifications.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.njangi.notifications.config.SecurityConfig;
import com.njangi.notifications.dto.EnvoyerNotificationRequest;
import com.njangi.notifications.dto.NotificationDto;
import com.njangi.notifications.entity.Canal;
import com.njangi.notifications.entity.StatutNotification;
import com.njangi.notifications.service.NotificationService;
import com.njangi.notifications.NotificationsApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@ContextConfiguration(classes = NotificationsApplication.class)
@Import(SecurityConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("POST /api/v1/notifications -> 201 Created")
    void testEnvoyer() throws Exception {
        UUID destId = UUID.randomUUID();
        UUID notifId = UUID.randomUUID();
        EnvoyerNotificationRequest request = new EnvoyerNotificationRequest(
                destId,
                UUID.randomUUID(),
                Canal.IN_APP,
                "TEST_TYPE",
                "Titre Test",
                "Contenu Test",
                null,
                null,
                null
        );

        NotificationDto dto = new NotificationDto(
                notifId,
                destId,
                request.groupeId(),
                Canal.IN_APP,
                "TEST_TYPE",
                "Titre Test",
                "Contenu Test",
                StatutNotification.ENVOYEE,
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                null
        );

        when(notificationService.envoyer(any(EnvoyerNotificationRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(notifId.toString()))
                .andExpect(jsonPath("$.data.statut").value("ENVOYEE"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/destinataire/{id} -> 200 OK")
    void testFindByDestinataire() throws Exception {
        UUID destId = UUID.randomUUID();
        NotificationDto dto = new NotificationDto(
                UUID.randomUUID(), destId, null, Canal.IN_APP,
                "TYPE", "Titre", "Contenu",
                StatutNotification.ENVOYEE, false,
                LocalDateTime.now(), LocalDateTime.now(),
                null, null, null
        );

        when(notificationService.findByDestinataire(destId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/notifications/destinataire/" + destId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].titre").value("Titre"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/destinataire/{id}/count-non-lues -> 200 OK")
    void testCountNonLues() throws Exception {
        UUID destId = UUID.randomUUID();
        when(notificationService.countNonLues(destId)).thenReturn(5L);

        mockMvc.perform(get("/api/v1/notifications/destinataire/" + destId + "/count-non-lues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nonLues").value(5));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/{id}/lue -> 200 OK")
    void testMarquerLue() throws Exception {
        UUID notifId = UUID.randomUUID();
        NotificationDto dto = new NotificationDto(
                notifId, UUID.randomUUID(), null, Canal.IN_APP,
                "TYPE", "Titre", "Contenu",
                StatutNotification.ENVOYEE, true,
                LocalDateTime.now(), LocalDateTime.now(),
                null, null, null
        );

        when(notificationService.marquerLue(notifId)).thenReturn(dto);

        mockMvc.perform(put("/api/v1/notifications/" + notifId + "/lue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lue").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/destinataire/{id}/tout-lire -> 200 OK")
    void testMarquerToutesLues() throws Exception {
        UUID destId = UUID.randomUUID();
        when(notificationService.marquerToutesLues(destId)).thenReturn(3);

        mockMvc.perform(put("/api/v1/notifications/destinataire/" + destId + "/tout-lire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.modifiees").value(3));
    }

    @Test
    @DisplayName("DELETE /api/v1/notifications/{id} -> 200 OK")
    void testSupprimer() throws Exception {
        UUID notifId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/notifications/" + notifId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationService).supprimer(notifId);
    }
}
