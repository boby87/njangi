package com.njangi.cotisations.controller;

import com.njangi.cotisations.dto.*;
import com.njangi.cotisations.service.CotisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cotisations")
@Tag(name = "Cotisations", description = "Gestion des cotisations individuelles et des appels de fonds par réunion")
public class CotisationController {

    private final CotisationService cotisationService;

    public CotisationController(CotisationService cotisationService) {
        this.cotisationService = cotisationService;
    }

    @PostMapping
    @Operation(summary = "Créer une cotisation individuelle manuellement")
    public ResponseEntity<ApiResponse<CotisationDto>> creerCotisation(
            @Valid @RequestBody CreateCotisationRequest request) {
        CotisationDto dto = cotisationService.creerCotisation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Cotisation creee avec succes"));
    }

    @PostMapping("/reunion/generer")
    @Operation(summary = "Générer automatiquement les cotisations obligatoires pour une réunion")
    public ResponseEntity<ApiResponse<List<CotisationDto>>> genererCotisationsPourReunion(
            @Valid @RequestBody GenererCotisationsReunionRequest request) {
        List<CotisationDto> dtos = cotisationService.genererCotisationsPourReunion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dtos, dtos.size() + " cotisations generees avec succes"));
    }

    @PutMapping("/{id}/paiement")
    @Operation(summary = "Enregistrer un paiement (partiel ou total) sur une cotisation")
    public ResponseEntity<ApiResponse<CotisationDto>> enregistrerPaiement(
            @PathVariable UUID id,
            @Valid @RequestBody MettreAJourPaiementCotisationRequest request) {
        CotisationDto dto = cotisationService.enregistrerPaiement(id, request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Paiement enregistre avec succes"));
    }

    @PutMapping("/{id}/retard")
    @Operation(summary = "Marquer une cotisation en retard")
    public ResponseEntity<ApiResponse<CotisationDto>> marquerEnRetard(@PathVariable UUID id) {
        CotisationDto dto = cotisationService.marquerEnRetard(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "Cotisation marquee en retard"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une cotisation par ID")
    public ResponseEntity<ApiResponse<CotisationDto>> obtenirParId(@PathVariable UUID id) {
        CotisationDto dto = cotisationService.obtenirParId(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/reunion/{reunionId}")
    @Operation(summary = "Lister les cotisations d'une réunion")
    public ResponseEntity<ApiResponse<List<CotisationDto>>> obtenirCotisationsParReunion(
            @PathVariable UUID reunionId) {
        List<CotisationDto> dtos = cotisationService.obtenirCotisationsParReunion(reunionId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/membre/{membreId}")
    @Operation(summary = "Lister les cotisations d'un membre")
    public ResponseEntity<ApiResponse<List<CotisationDto>>> obtenirCotisationsParMembre(
            @PathVariable UUID membreId) {
        List<CotisationDto> dtos = cotisationService.obtenirCotisationsParMembre(membreId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/groupe/{groupeId}")
    @Operation(summary = "Lister les cotisations d'un groupe")
    public ResponseEntity<ApiResponse<List<CotisationDto>>> obtenirCotisationsParGroupe(
            @PathVariable UUID groupeId) {
        List<CotisationDto> dtos = cotisationService.obtenirCotisationsParGroupe(groupeId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}
