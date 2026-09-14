package com.njangi.notifications.service;

import com.njangi.notifications.dto.EnvoyerNotificationRequest;
import com.njangi.notifications.dto.NotificationDto;
import com.njangi.notifications.entity.Canal;
import com.njangi.notifications.entity.Notification;
import com.njangi.notifications.entity.StatutNotification;
import com.njangi.notifications.event.NotificationEventPublisher;
import com.njangi.notifications.exception.NotificationNotFoundException;
import com.njangi.notifications.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationEventPublisher eventPublisher;
    private final EmailService emailService;
    private final SmsService smsService;
    private final FirebaseMessagingService firebaseMessagingService;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationEventPublisher eventPublisher,
                               EmailService emailService,
                               SmsService smsService,
                               FirebaseMessagingService firebaseMessagingService) {
        this.notificationRepository = notificationRepository;
        this.eventPublisher = eventPublisher;
        this.emailService = emailService;
        this.smsService = smsService;
        this.firebaseMessagingService = firebaseMessagingService;
    }

    public NotificationDto envoyer(EnvoyerNotificationRequest request) {
        log.info("Création et routage d'une notification canal={} type={} pour destinataire={}",
                request.canal(), request.type(), request.destinataireId());

        Notification notification = new Notification();
        notification.setDestinataireId(request.destinataireId());
        notification.setGroupeId(request.groupeId());
        notification.setCanal(request.canal());
        notification.setType(request.type());
        notification.setTitre(request.titre());
        notification.setContenu(request.contenu());
        notification.setReferenceObjet(request.referenceObjet());
        notification.setTypeObjet(request.typeObjet());
        notification.setDestinataireContact(request.destinataireContact());
        notification.setStatut(StatutNotification.EN_ATTENTE);
        notification.setLue(false);
        notification.setCreeLe(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);

        boolean succes = expedier(saved);

        if (succes) {
            saved.setStatut(StatutNotification.ENVOYEE);
            saved.setEnvoyeeLe(LocalDateTime.now());
        } else {
            saved.setStatut(StatutNotification.ECHEC);
        }

        Notification updated = notificationRepository.save(saved);

        try {
            eventPublisher.publishNotificationEnvoyee(updated);
        } catch (Exception e) {
            log.warn("Impossible de publier l'événement Kafka pour la notification {} : {}", updated.getId(), e.getMessage());
        }

        return NotificationDto.from(updated);
    }

    private boolean expedier(Notification n) {
        Canal canal = n.getCanal();
        return switch (canal) {
            case IN_APP -> true;
            case SMS -> smsService.envoyerSms(n.getDestinataireContact(), n.getTitre() + " : " + n.getContenu());
            case EMAIL -> emailService.envoyerEmail(n.getDestinataireContact(), n.getTitre(), n.getContenu());
            case PUSH -> firebaseMessagingService.envoyerPush(
                    n.getDestinataireContact(),
                    n.getTitre(),
                    n.getContenu(),
                    Map.of("type", n.getType(), "id", n.getId().toString())
            );
        };
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> findByDestinataire(UUID destinataireId) {
        return notificationRepository.findByDestinataireIdOrderByCreeLeDesc(destinataireId)
                .stream().map(NotificationDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> findNonLues(UUID destinataireId) {
        return notificationRepository.findByDestinataireIdAndLueFalseOrderByCreeLeDesc(destinataireId)
                .stream().map(NotificationDto::from).toList();
    }

    @Transactional(readOnly = true)
    public long countNonLues(UUID destinataireId) {
        return notificationRepository.countByDestinataireIdAndLueFalse(destinataireId);
    }

    public NotificationDto marquerLue(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        notification.setLue(true);
        return NotificationDto.from(notificationRepository.save(notification));
    }

    public int marquerToutesLues(UUID destinataireId) {
        return notificationRepository.marquerToutesLues(destinataireId);
    }

    public void supprimer(UUID id) {
        if (!notificationRepository.existsById(id)) {
            throw new NotificationNotFoundException(id);
        }
        notificationRepository.deleteById(id);
    }
}
