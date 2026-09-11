package com.njangi.notifications.service;

import com.njangi.notifications.dto.NotificationDto;
import com.njangi.notifications.entity.Notification;
import com.njangi.notifications.entity.Notification.StatutNotification;
import com.njangi.notifications.exception.NotificationNotFoundException;
import com.njangi.notifications.kafka.NotificationEventPublisher;
import com.njangi.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationEventPublisher eventPublisher;

    /**
     * Crée et déclenche l'envoi d'une notification.
     * L'envoi réel (FCM, SMS, Email) est asynchrone via l'événement Kafka "notification.demandee".
     * Les notifications IN_APP sont marquées ENVOYEE immédiatement.
     */
    public NotificationDto envoyer(NotificationDto dto) {
        log.info("Création d'une notification canal={} pour destinataire={}", dto.canal(), dto.destinatreId());
        Notification notification = Notification.builder()
                .destinatreId(dto.destinatreId())
                .groupeId(dto.groupeId())
                .canal(dto.canal())
                .type(dto.type())
                .titre(dto.titre())
                .contenu(dto.contenu())
                .statut(StatutNotification.EN_ATTENTE)
                .lue(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        if (dto.canal() == Notification.Canal.IN_APP) {
            // Livraison directe pour IN_APP — pas besoin de canal externe
            saved.setStatut(StatutNotification.ENVOYEE);
            saved.setEnvoyeeLe(LocalDateTime.now());
            saved = notificationRepository.save(saved);
        } else {
            // Déléguer l'envoi aux canaux externes via Kafka
            eventPublisher.publishNotificationDemandee(saved);
        }

        log.info("Notification créée avec id={} canal={}", saved.getId(), saved.getCanal());
        return NotificationDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> findNonLues(UUID destinatreId) {
        return notificationRepository.findNonLuesByDestinatreId(destinatreId)
                .stream().map(NotificationDto::from).toList();
    }

    public NotificationDto marquerLue(UUID id) {
        log.info("Marquage de la notification id={} comme lue", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        notification.setLue(true);
        return NotificationDto.from(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> findByDestinataire(UUID destinatreId) {
        return notificationRepository.findByDestinatreIdOrderByCreeLe(destinatreId)
                .stream().map(NotificationDto::from).toList();
    }

    @Transactional(readOnly = true)
    public long countNonLues(UUID destinatreId) {
        return notificationRepository.countNonLuesByDestinatreId(destinatreId);
    }
}
