package com.njangi.notifications.controller;

import com.njangi.notifications.dto.ApiResponse;
import com.njangi.notifications.dto.EnvoyerNotificationRequest;
import com.njangi.notifications.dto.NotificationDto;
import com.njangi.notifications.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "API d'envoi et de consultation des notifications (Push FCM, SMS, Email, In-App)")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "Envoyer une notification", description = "Crée et achemine une notification via le canal spécifié (PUSH, SMS, EMAIL ou IN_APP)")
    public ResponseEntity<ApiResponse<NotificationDto>> envoyer(@Valid @RequestBody EnvoyerNotificationRequest request) {
        NotificationDto dto = notificationService.envoyer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Notification envoyée avec succès"));
    }

    @GetMapping("/destinataire/{destinataireId}")
    @Operation(summary = "Historique des notifications d'un membre", description = "Retourne la liste chronologique des notifications d'un membre")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> findByDestinataire(@PathVariable UUID destinataireId) {
        List<NotificationDto> notifications = notificationService.findByDestinataire(destinataireId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/destinataire/{destinataireId}/non-lues")
    @Operation(summary = "Notifications non lues d'un membre", description = "Retourne les notifications non lues d'un membre pour affichage en badge")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> findNonLues(@PathVariable UUID destinataireId) {
        List<NotificationDto> nonLues = notificationService.findNonLues(destinataireId);
        return ResponseEntity.ok(ApiResponse.success(nonLues));
    }

    @GetMapping("/destinataire/{destinataireId}/count-non-lues")
    @Operation(summary = "Nombre de notifications non lues", description = "Compteur rapide pour les icônes de cloche sur le web et mobile")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countNonLues(@PathVariable UUID destinataireId) {
        long count = notificationService.countNonLues(destinataireId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("nonLues", count)));
    }

    @PutMapping("/{id}/lue")
    @Operation(summary = "Marquer une notification comme lue", description = "Met à jour l'état de lecture d'une notification")
    public ResponseEntity<ApiResponse<NotificationDto>> marquerLue(@PathVariable UUID id) {
        NotificationDto dto = notificationService.marquerLue(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "Notification marquée comme lue"));
    }

    @PutMapping("/destinataire/{destinataireId}/tout-lire")
    @Operation(summary = "Marquer toutes les notifications comme lues", description = "Passe toutes les notifications non lues d'un membre à l'état lu")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> marquerToutesLues(@PathVariable UUID destinataireId) {
        int count = notificationService.marquerToutesLues(destinataireId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("modifiees", count), "Toutes les notifications ont été marquées comme lues"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une notification", description = "Supprime définitivement une notification de l'historique")
    public ResponseEntity<ApiResponse<Void>> supprimer(@PathVariable UUID id) {
        notificationService.supprimer(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification supprimée avec succès"));
    }
}
