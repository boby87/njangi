package com.njangi.groupes.controller;

import com.njangi.groupes.dto.ApiResponse;
import com.njangi.groupes.dto.ElireBureauRequest;
import com.njangi.groupes.dto.MandatBureauDto;
import com.njangi.groupes.service.MandatBureauService;
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
@Tag(name = "Gouvernance & Mandats de Bureau", description = "Gestion des élections de bureau exécutif et mandats (Président, Trésorier, Secrétaire)")
public class MandatBureauController {

    private final MandatBureauService mandatService;

    public MandatBureauController(MandatBureauService mandatService) {
        this.mandatService = mandatService;
    }

    @GetMapping("/{groupeId}/bureau/actif")
    @Operation(summary = "Consulter le bureau exécutif en exercice du groupe")
    public ResponseEntity<ApiResponse<MandatBureauDto>> getMandatActif(@PathVariable UUID groupeId) {
        return mandatService.getMandatActif(groupeId)
                .map(m -> ResponseEntity.ok(ApiResponse.ok(m)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok("Aucun bureau élu en exercice", null)));
    }

    @GetMapping("/{groupeId}/bureau/historique")
    @Operation(summary = "Consulter l'historique de tous les mandats de bureau passés et présents")
    public ResponseEntity<ApiResponse<List<MandatBureauDto>>> getHistoriqueMandats(@PathVariable UUID groupeId) {
        return ResponseEntity.ok(ApiResponse.ok(mandatService.getHistoriqueMandats(groupeId)));
    }

    @PostMapping("/{groupeId}/bureau/election")
    @Operation(summary = "Élire un nouveau bureau exécutif (rétrograde automatiquement le Créateur en simple membre)")
    public ResponseEntity<ApiResponse<MandatBureauDto>> elireBureau(
            @PathVariable UUID groupeId,
            @Valid @RequestBody ElireBureauRequest request) {
        MandatBureauDto dto = mandatService.elireBureau(groupeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok("Bureau élu avec succès. Les rôles et prérogatives ont été mis à jour.", dto)
        );
    }
}
