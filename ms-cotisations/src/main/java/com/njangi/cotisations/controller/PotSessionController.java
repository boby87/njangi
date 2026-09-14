package com.njangi.cotisations.controller;

import com.njangi.cotisations.dto.*;
import com.njangi.cotisations.service.PotSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cotisations/pots")
@Tag(name = "Tours de Pot & Cagnottes", description = "Gestion du cycle de pot rotatif, ordre de passage, attribution et décaissement")
public class PotSessionController {

    private final PotSessionService potSessionService;

    public PotSessionController(PotSessionService potSessionService) {
        this.potSessionService = potSessionService;
    }

    @PostMapping("/planifier")
    @Operation(summary = "Planifier le tour de passage du pot pour une session complète")
    public ResponseEntity<ApiResponse<List<PotSessionDto>>> planifierTourPot(
            @Valid @RequestBody PlanifierTourPotRequest request) {
        List<PotSessionDto> dtos = potSessionService.planifierTourPot(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dtos, dtos.size() + " tours de pot planifiés avec succès"));
    }

    @PutMapping("/{id}/attribuer")
    @Operation(summary = "Attribuer le pot rotatif à un bénéficiaire lors d'une réunion")
    public ResponseEntity<ApiResponse<PotSessionDto>> attribuerPot(
            @PathVariable UUID id,
            @Valid @RequestBody AttribuerPotRequest request) {
        PotSessionDto dto = potSessionService.attribuerPot(id, request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Pot attribué avec succès"));
    }

    @PutMapping("/{id}/decaisser")
    @Operation(summary = "Enregistrer le décaissement du pot par le trésorier (Cash ou Mobile Money)")
    public ResponseEntity<ApiResponse<PotSessionDto>> decaisserPot(
            @PathVariable UUID id,
            @Valid @RequestBody DecaisserPotRequest request) {
        PotSessionDto dto = potSessionService.decaisserPot(id, request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Décaissement du pot enregistré avec succès"));
    }

    @GetMapping("/session/{sessionTontineId}")
    @Operation(summary = "Obtenir les tours de pot d'une session ordonnés par tour de passage")
    public ResponseEntity<ApiResponse<List<PotSessionDto>>> obtenirPotsParSession(
            @PathVariable UUID sessionTontineId) {
        List<PotSessionDto> dtos = potSessionService.obtenirPotsParSession(sessionTontineId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/groupe/{groupeId}")
    @Operation(summary = "Obtenir tous les pots d'un groupe")
    public ResponseEntity<ApiResponse<List<PotSessionDto>>> obtenirPotsParGroupe(
            @PathVariable UUID groupeId) {
        List<PotSessionDto> dtos = potSessionService.obtenirPotsParGroupe(groupeId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/membre/{membreId}")
    @Operation(summary = "Obtenir les pots bénéficiés par un membre")
    public ResponseEntity<ApiResponse<List<PotSessionDto>>> obtenirPotsParMembre(
            @PathVariable UUID membreId) {
        List<PotSessionDto> dtos = potSessionService.obtenirPotsParMembre(membreId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir le détail d'un tour de pot par ID")
    public ResponseEntity<ApiResponse<PotSessionDto>> obtenirParId(@PathVariable UUID id) {
        PotSessionDto dto = potSessionService.obtenirParId(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
