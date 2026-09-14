package com.njangi.groupes.controller;

import com.njangi.groupes.dto.ApiResponse;
import com.njangi.groupes.dto.CreerSessionRequest;
import com.njangi.groupes.dto.SessionTontineDto;
import com.njangi.groupes.service.SessionTontineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groupes")
@Tag(name = "Sessions Tontinières", description = "Gestion des cycles annuels/périodiques d'un tour de cagnotte rotative")
public class SessionTontineController {

    private final SessionTontineService sessionService;

    public SessionTontineController(SessionTontineService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/{groupeId}/sessions")
    @Operation(summary = "Lister toutes les sessions tontinières d'un groupe")
    public ResponseEntity<ApiResponse<List<SessionTontineDto>>> getSessions(@PathVariable UUID groupeId) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.getSessions(groupeId)));
    }

    @GetMapping("/{groupeId}/sessions/active")
    @Operation(summary = "Obtenir la session active (EN_COURS) du groupe")
    public ResponseEntity<ApiResponse<SessionTontineDto>> getSessionActive(@PathVariable UUID groupeId) {
        return sessionService.getSessionActive(groupeId)
                .map(s -> ResponseEntity.ok(ApiResponse.ok(s)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok("Aucune session active", null)));
    }

    @PostMapping("/{groupeId}/sessions")
    @Operation(summary = "Planifier une nouvelle session tontinière pour le groupe")
    public ResponseEntity<ApiResponse<SessionTontineDto>> creerSession(
            @PathVariable UUID groupeId,
            @Valid @RequestBody CreerSessionRequest request) {
        SessionTontineDto dto = sessionService.creerSession(groupeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Session planifiée avec succès", dto));
    }

    @PutMapping("/sessions/{sessionId}/demarrer")
    @Operation(summary = "Démarrer officiellement une session tontinière (EN_COURS)")
    public ResponseEntity<ApiResponse<SessionTontineDto>> demarrerSession(@PathVariable UUID sessionId) {
        SessionTontineDto dto = sessionService.demarrerSession(sessionId);
        return ResponseEntity.ok(ApiResponse.ok("Session démarrée avec succès", dto));
    }

    @PutMapping("/sessions/{sessionId}/cloturer")
    @Operation(summary = "Clôturer définitivement une session tontinière (CLOTUREE)")
    public ResponseEntity<ApiResponse<SessionTontineDto>> cloturerSession(@PathVariable UUID sessionId) {
        SessionTontineDto dto = sessionService.cloturerSession(sessionId);
        return ResponseEntity.ok(ApiResponse.ok("Session clôturée avec succès", dto));
    }
}
