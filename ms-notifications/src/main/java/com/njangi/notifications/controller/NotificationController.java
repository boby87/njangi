package com.njangi.notifications.controller;

import com.njangi.notifications.dto.NotificationDto;
import com.njangi.notifications.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationDto> envoyer(@Valid @RequestBody NotificationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.envoyer(dto));
    }

    @GetMapping("/destinataire/{destinatreId}")
    public ResponseEntity<List<NotificationDto>> findByDestinataire(@PathVariable UUID destinatreId) {
        return ResponseEntity.ok(notificationService.findByDestinataire(destinatreId));
    }

    @GetMapping("/destinataire/{destinatreId}/non-lues")
    public ResponseEntity<List<NotificationDto>> findNonLues(@PathVariable UUID destinatreId) {
        return ResponseEntity.ok(notificationService.findNonLues(destinatreId));
    }

    @GetMapping("/destinataire/{destinatreId}/count-non-lues")
    public ResponseEntity<Map<String, Long>> countNonLues(@PathVariable UUID destinatreId) {
        long count = notificationService.countNonLues(destinatreId);
        return ResponseEntity.ok(Map.of("nonLues", count));
    }

    @PutMapping("/{id}/lue")
    public ResponseEntity<NotificationDto> marquerLue(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.marquerLue(id));
    }
}
