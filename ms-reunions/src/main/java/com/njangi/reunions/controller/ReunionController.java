package com.njangi.reunions.controller;

import com.njangi.reunions.dto.*;
import com.njangi.reunions.entity.StatutReunion;
import com.njangi.reunions.service.PresenceService;
import com.njangi.reunions.service.ReunionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reunions")
@Tag(name = "Réunions & Séances", description = "Gestion des réunions de tontine, ordre du jour, émargement des présences et procès-verbaux")
public class ReunionController {

    private final ReunionService reunionService;
    private final PresenceService presenceService;

    public ReunionController(ReunionService reunionService, PresenceService presenceService) {
        this.reunionService = reunionService;
        this.presenceService = presenceService;
    }

    @GetMapping
    @Operation(summary = "Lister toutes les réunions")
    public ResponseEntity<ApiResponse<List<ReunionDto>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(reunionService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter les détails d'une réunion par son identifiant")
    public ResponseEntity<ApiResponse<ReunionDto>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(reunionService.findById(id)));
    }

    @GetMapping("/groupe/{groupeId}")
    @Operation(summary = "Lister les réunions d'un groupe ordonnées par date décroissante")
    public ResponseEntity<ApiResponse<List<ReunionDto>>> findByGroupe(@PathVariable UUID groupeId) {
        return ResponseEntity.ok(ApiResponse.ok(reunionService.findByGroupe(groupeId)));
    }

    @GetMapping("/groupe/{groupeId}/statut/{statut}")
    @Operation(summary = "Filtrer les réunions d'un groupe par statut")
    public ResponseEntity<ApiResponse<List<ReunionDto>>> findByGroupeAndStatut(
            @PathVariable UUID groupeId,
            @PathVariable StatutReunion statut) {
        return ResponseEntity.ok(ApiResponse.ok(reunionService.findByGroupeAndStatut(groupeId, statut)));
    }

    @PostMapping
    @Operation(summary = "Planifier une nouvelle réunion de tontine")
    public ResponseEntity<ApiResponse<ReunionDto>> create(@Valid @RequestBody CreateReunionRequest request) {
        ReunionDto dto = reunionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Réunion planifiée avec succès", dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une réunion existante")
    public ResponseEntity<ApiResponse<ReunionDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReunionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Réunion mise à jour", reunionService.update(id, request)));
    }

    @PutMapping("/{id}/demarrer")
    @Operation(summary = "Démarrer la séance en direct (passe le statut à EN_COURS)")
    public ResponseEntity<ApiResponse<ReunionDto>> demarrer(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Séance démarrée en direct", reunionService.demarrerReunion(id)));
    }

    @PutMapping("/{id}/cloturer")
    @Operation(summary = "Clôturer la séance et enregistrer le procès-verbal final (passe à TERMINEE)")
    public ResponseEntity<ApiResponse<ReunionDto>> cloturer(
            @PathVariable UUID id,
            @RequestBody(required = false) CloturerReunionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Réunion clôturée avec succès", reunionService.terminerReunion(id, request)));
    }

    @PutMapping("/{id}/annuler")
    @Operation(summary = "Annuler une réunion")
    public ResponseEntity<ApiResponse<ReunionDto>> annuler(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Réunion annulée", reunionService.annulerReunion(id)));
    }

    @PutMapping("/{id}/ordre-du-jour")
    @Operation(summary = "Mettre à jour l'ordre du jour officiel (par le Président)")
    public ResponseEntity<ApiResponse<ReunionDto>> updateOrdreJour(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrdreJourRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Ordre du jour mis à jour", reunionService.updateOrdreJour(id, request.ordreJour())));
    }

    @PutMapping("/{id}/compte-rendu")
    @Operation(summary = "Rédiger ou modifier le compte-rendu / PV (par le Secrétaire)")
    public ResponseEntity<ApiResponse<ReunionDto>> updateCompteRendu(
            @PathVariable UUID id,
            @RequestBody CloturerReunionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Compte-rendu mis à jour", reunionService.updateCompteRendu(id, request.compteRendu())));
    }

    @GetMapping("/{id}/quorum")
    @Operation(summary = "Calculer le quorum et statistiques de présence de la séance")
    public ResponseEntity<ApiResponse<QuorumDto>> calculerQuorum(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(presenceService.calculerQuorum(id)));
    }

    @GetMapping("/{id}/presences")
    @Operation(summary = "Consulter le registre d'émargement des présences d'une réunion")
    public ResponseEntity<ApiResponse<List<PresenceDto>>> getPresences(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(presenceService.getPresences(id)));
    }

    @PostMapping("/{id}/presences")
    @Operation(summary = "Enregistrer la présence d'un membre individuel")
    public ResponseEntity<ApiResponse<PresenceDto>> enregistrerPresence(
            @PathVariable UUID id,
            @Valid @RequestBody EnregistrerPresenceRequest request) {
        PresenceDto dto = presenceService.enregistrerPresence(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Présence enregistrée", dto));
    }

    @PostMapping("/{id}/presences/batch")
    @Operation(summary = "Émarger l'appel complet des membres en une seule requête (Batch)")
    public ResponseEntity<ApiResponse<List<PresenceDto>>> enregistrerPresencesBatch(
            @PathVariable UUID id,
            @Valid @RequestBody BatchPresencesRequest request) {
        List<PresenceDto> list = presenceService.enregistrerPresencesBatch(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Émargement par lot enregistré", list));
    }
}
