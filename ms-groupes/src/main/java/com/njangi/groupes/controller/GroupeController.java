package com.njangi.groupes.controller;

import com.njangi.groupes.dto.*;
import com.njangi.groupes.service.GroupeService;
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
@Tag(name = "Groupes de Tontine", description = "Gestion des tontines, sièges (fixe/rotatif), invitations et adhésions")
public class GroupeController {

    private final GroupeService groupeService;

    public GroupeController(GroupeService groupeService) {
        this.groupeService = groupeService;
    }

    @GetMapping
    @Operation(summary = "Lister toutes les tontines")
    public ResponseEntity<ApiResponse<List<GroupeDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(groupeService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter les informations d'un groupe par son ID")
    public ResponseEntity<ApiResponse<GroupeDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(groupeService.findById(id)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Rechercher une tontine via son code d'invitation")
    public ResponseEntity<ApiResponse<GroupeDto>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(groupeService.findByCodeInvitation(code)));
    }

    @GetMapping("/createur/{createurId}")
    @Operation(summary = "Lister les tontines initiées par un créateur")
    public ResponseEntity<ApiResponse<List<GroupeDto>>> getByCreateur(@PathVariable UUID createurId) {
        return ResponseEntity.ok(ApiResponse.ok(groupeService.findByCreateur(createurId)));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau groupe de tontine (enregistre le créateur)")
    public ResponseEntity<ApiResponse<GroupeDto>> create(@Valid @RequestBody CreerGroupeRequest request) {
        GroupeDto dto = groupeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Tontine créée avec succès", dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier les paramètres d'un groupe existant")
    public ResponseEntity<ApiResponse<GroupeDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ModifierGroupeRequest request) {
        GroupeDto dto = groupeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Paramètres de la tontine mis à jour", dto));
    }

    @GetMapping("/{id}/membres")
    @Operation(summary = "Lister tous les membres adhérents à une tontine")
    public ResponseEntity<ApiResponse<List<GroupeMembreDto>>> getMembres(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(groupeService.getMembres(id)));
    }

    @PostMapping("/{id}/membres")
    @Operation(summary = "Ajouter manuellement un membre à une tontine")
    public ResponseEntity<ApiResponse<GroupeMembreDto>> ajouterMembre(
            @PathVariable UUID id,
            @Valid @RequestBody AjouterMembreRequest request) {
        GroupeMembreDto dto = groupeService.ajouterMembre(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Membre ajouté avec succès", dto));
    }

    @PostMapping("/rejoindre")
    @Operation(summary = "Rejoindre une tontine en saisissant un code d'invitation")
    public ResponseEntity<ApiResponse<GroupeMembreDto>> rejoindreParCode(
            @RequestParam String codeInvitation,
            @RequestParam UUID membreId) {
        GroupeMembreDto dto = groupeService.rejoindreParCode(codeInvitation, membreId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Vous avez rejoint la tontine avec succès", dto));
    }
}
