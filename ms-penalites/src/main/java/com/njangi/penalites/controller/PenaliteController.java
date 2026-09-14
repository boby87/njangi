package com.njangi.penalites.controller;

import com.njangi.penalites.dto.ApiResponse;
import com.njangi.penalites.dto.InfligerPenaliteRequest;
import com.njangi.penalites.dto.PenaliteDto;
import com.njangi.penalites.service.PenaliteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/penalites")
@Tag(name = "Pénalités & Sanctions", description = "Application des sanctions disciplinaires, suivi des paiements et des annulations")
public class PenaliteController {

    private final PenaliteService penaliteService;

    public PenaliteController(PenaliteService penaliteService) {
        this.penaliteService = penaliteService;
    }

    @PostMapping
    @Operation(summary = "Infliger une pénalité à un membre (résolution automatique du montant via le barème)")
    public ResponseEntity<ApiResponse<PenaliteDto>> infligerPenalite(
            @Valid @RequestBody InfligerPenaliteRequest request) {
        PenaliteDto dto = penaliteService.infligerPenalite(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Pénalité infligée avec succès"));
    }

    @PutMapping("/{id}/payer")
    @Operation(summary = "Enregistrer le règlement d'une pénalité")
    public ResponseEntity<ApiResponse<PenaliteDto>> payer(@PathVariable UUID id) {
        PenaliteDto dto = penaliteService.payer(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "Règlement de la pénalité enregistré avec succès"));
    }

    @PutMapping("/{id}/annuler")
    @Operation(summary = "Annuler ou dispenser une pénalité avec motif")
    public ResponseEntity<ApiResponse<PenaliteDto>> annuler(
            @PathVariable UUID id,
            @RequestParam(required = false) String motif) {
        PenaliteDto dto = penaliteService.annuler(id, motif);
        return ResponseEntity.ok(ApiResponse.success(dto, "Pénalité annulée avec succès"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'une pénalité par son ID")
    public ResponseEntity<ApiResponse<PenaliteDto>> obtenirParId(@PathVariable UUID id) {
        PenaliteDto dto = penaliteService.obtenirParId(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/membre/{membreId}")
    @Operation(summary = "Lister les pénalités d'un membre")
    public ResponseEntity<ApiResponse<List<PenaliteDto>>> obtenirParMembre(@PathVariable UUID membreId) {
        List<PenaliteDto> dtos = penaliteService.obtenirParMembre(membreId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/groupe/{groupeId}")
    @Operation(summary = "Lister les pénalités appliquées dans un groupe")
    public ResponseEntity<ApiResponse<List<PenaliteDto>>> obtenirParGroupe(@PathVariable UUID groupeId) {
        List<PenaliteDto> dtos = penaliteService.obtenirParGroupe(groupeId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/reunion/{reunionId}")
    @Operation(summary = "Lister les pénalités infligées lors d'une séance de réunion")
    public ResponseEntity<ApiResponse<List<PenaliteDto>>> obtenirParReunion(@PathVariable UUID reunionId) {
        List<PenaliteDto> dtos = penaliteService.obtenirParReunion(reunionId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Lister les pénalités appliquées sur l'ensemble d'une session de tontine")
    public ResponseEntity<ApiResponse<List<PenaliteDto>>> obtenirParSession(@PathVariable UUID sessionId) {
        List<PenaliteDto> dtos = penaliteService.obtenirParSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}
