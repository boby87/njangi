package com.njangi.notifications.service;

import com.njangi.notifications.dto.EnvoyerNotificationRequest;
import com.njangi.notifications.dto.NotificationDto;
import com.njangi.notifications.entity.Canal;
import com.njangi.notifications.entity.Notification;
import com.njangi.notifications.entity.StatutNotification;
import com.njangi.notifications.event.NotificationEventPublisher;
import com.njangi.notifications.exception.NotificationNotFoundException;
import com.njangi.notifications.repository.NotificationRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationEventPublisher eventPublisher;

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    @Mock
    private FirebaseMessagingService firebaseMessagingService;

    private NotificationService notificationService;

    private UUID destinataireId;
    private UUID groupeId;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                eventPublisher,
                emailService,
                smsService,
                firebaseMessagingService
        );
        destinataireId = UUID.randomUUID();
        groupeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Envoi d'une notification IN_APP : livraison immédiate ENVOYEE")
    void testEnvoyerNotificationInApp() {
        EnvoyerNotificationRequest req = new EnvoyerNotificationRequest(
                destinataireId,
                groupeId,
                Canal.IN_APP,
                "COTISATION_PAYEE",
                "Cotisation reçue",
                "Votre cotisation a bien été reçue.",
                UUID.randomUUID().toString(),
                "COTISATION",
                null
        );

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            if (n.getId() == null) {
                n.setId(UUID.randomUUID());
            }
            return n;
        });

        NotificationDto result = notificationService.envoyer(req);

        assertThat(result).isNotNull();
        assertThat(result.canal()).isEqualTo(Canal.IN_APP);
        assertThat(result.statut()).isEqualTo(StatutNotification.ENVOYEE);
        assertThat(result.lue()).isFalse();
        assertThat(result.titre()).isEqualTo("Cotisation reçue");
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(eventPublisher, times(1)).publishNotificationEnvoyee(any(Notification.class));
    }

    @Test
    @DisplayName("Envoi d'une notification SMS : acheminé via SmsService")
    void testEnvoyerNotificationSms() {
        EnvoyerNotificationRequest req = new EnvoyerNotificationRequest(
                destinataireId,
                groupeId,
                Canal.SMS,
                "RAPPEL_REUNION",
                "Rappel Réunion",
                "Séance samedi à 15h.",
                null,
                null,
                "+237699001122"
        );

        when(smsService.envoyerSms(eq("+237699001122"), anyString())).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            if (n.getId() == null) {
                n.setId(UUID.randomUUID());
            }
            return n;
        });

        NotificationDto result = notificationService.envoyer(req);

        assertThat(result.statut()).isEqualTo(StatutNotification.ENVOYEE);
        verify(smsService, times(1)).envoyerSms(eq("+237699001122"), anyString());
    }

    @Test
    @DisplayName("Envoi d'une notification EMAIL : acheminé via EmailService")
    void testEnvoyerNotificationEmail() {
        EnvoyerNotificationRequest req = new EnvoyerNotificationRequest(
                destinataireId,
                groupeId,
                Canal.EMAIL,
                "CONVOCATION",
                "Convocation Assemblée Générale",
                "Veuillez trouver l'ordre du jour...",
                null,
                null,
                "membre@njangi.cm"
        );

        when(emailService.envoyerEmail(eq("membre@njangi.cm"), eq("Convocation Assemblée Générale"), anyString())).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            if (n.getId() == null) {
                n.setId(UUID.randomUUID());
            }
            return n;
        });

        NotificationDto result = notificationService.envoyer(req);

        assertThat(result.statut()).isEqualTo(StatutNotification.ENVOYEE);
        verify(emailService, times(1)).envoyerEmail(eq("membre@njangi.cm"), eq("Convocation Assemblée Générale"), anyString());
    }

    @Test
    @DisplayName("Envoi d'une notification PUSH FCM : acheminé via FirebaseMessagingService")
    void testEnvoyerNotificationPush() {
        EnvoyerNotificationRequest req = new EnvoyerNotificationRequest(
                destinataireId,
                groupeId,
                Canal.PUSH,
                "POT_ATTRIBUE",
                "Tour de pot !",
                "Vous remportez le pot ce mois-ci.",
                null,
                null,
                "fcm_token_device_xyz"
        );

        when(firebaseMessagingService.envoyerPush(eq("fcm_token_device_xyz"), eq("Tour de pot !"), anyString(), anyMap())).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            if (n.getId() == null) {
                n.setId(UUID.randomUUID());
            }
            return n;
        });

        NotificationDto result = notificationService.envoyer(req);

        assertThat(result.statut()).isEqualTo(StatutNotification.ENVOYEE);
        verify(firebaseMessagingService, times(1)).envoyerPush(eq("fcm_token_device_xyz"), eq("Tour de pot !"), anyString(), anyMap());
    }

    @Test
    @DisplayName("Marquage d'une notification comme lue")
    void testMarquerLue() {
        UUID notifId = UUID.randomUUID();
        Notification n = new Notification();
        n.setId(notifId);
        n.setDestinataireId(destinataireId);
        n.setCanal(Canal.IN_APP);
        n.setType("TEST");
        n.setTitre("Test");
        n.setContenu("Contenu");
        n.setStatut(StatutNotification.ENVOYEE);
        n.setLue(false);
        n.setCreeLe(LocalDateTime.now());

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationDto result = notificationService.marquerLue(notifId);

        assertThat(result.lue()).isTrue();
        verify(notificationRepository).save(argThat(Notification::isLue));
    }

    @Test
    @DisplayName("Marquer lue notification inexistante lève NotificationNotFoundException")
    void testMarquerLueInexistante() {
        UUID notifId = UUID.randomUUID();
        when(notificationRepository.findById(notifId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.marquerLue(notifId))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    @DisplayName("Marquage de toutes les notifications d'un membre comme lues")
    void testMarquerToutesLues() {
        when(notificationRepository.marquerToutesLues(destinataireId)).thenReturn(4);

        int count = notificationService.marquerToutesLues(destinataireId);

        assertThat(count).isEqualTo(4);
        verify(notificationRepository).marquerToutesLues(destinataireId);
    }

    @Test
    @DisplayName("Comptage et liste des notifications non lues")
    void testFindEtCountNonLues() {
        Notification n1 = new Notification();
        n1.setId(UUID.randomUUID());
        n1.setDestinataireId(destinataireId);
        n1.setCanal(Canal.IN_APP);
        n1.setType("T1");
        n1.setTitre("Titre 1");
        n1.setContenu("C1");
        n1.setLue(false);
        n1.setCreeLe(LocalDateTime.now());

        when(notificationRepository.findByDestinataireIdAndLueFalseOrderByCreeLeDesc(destinataireId)).thenReturn(List.of(n1));
        when(notificationRepository.countByDestinataireIdAndLueFalse(destinataireId)).thenReturn(1L);

        List<NotificationDto> nonLues = notificationService.findNonLues(destinataireId);
        long count = notificationService.countNonLues(destinataireId);

        assertThat(nonLues).hasSize(1);
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("Suppression d'une notification existante")
    void testSupprimer() {
        UUID notifId = UUID.randomUUID();
        when(notificationRepository.existsById(notifId)).thenReturn(true);

        notificationService.supprimer(notifId);

        verify(notificationRepository).deleteById(notifId);
    }
}
