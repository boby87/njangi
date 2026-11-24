package com.njangi.finances.cotisation.controller;

import com.njangi.finances.common.ApiResponse;
import com.njangi.finances.cotisation.dto.CreerTypeCotisationRequest;
import com.njangi.finances.cotisation.dto.TypeCotisationDto;
import com.njangi.finances.cotisation.service.TypeCotisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cotisations/types")
@Tag(name = "Types de Cotisation", description = "Gestion des caisses multi-cotisations (pot rotatif, secours, caisse de réserve)")
public class TypeCotisationController {

    private final TypeCotisationService typeCotisationService;

    public TypeCotisationController(TypeCotisationService typeCotisationService) {
        this.typeCotisationService = typeCotisationService;
    }

    @PostMapping
    @Operation(summary = "Créer un type de cotisation pour un groupe")
    public ResponseEntity<ApiResponse<TypeCotisationDto>> creerTypeCotisation(
            @Valid @RequestBody CreerTypeCotisationRequest request) {
        TypeCotisationDto dto = typeCotisationService.creerTypeCotisation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Type de cotisation créé avec succès"));
    }

    @GetMapping("/groupe/{groupeId}")
    @Operation(summary = "Lister tous les types de cotisation d'un groupe")
    public ResponseEntity<ApiResponse<List<TypeCotisationDto>>> obtenirTypesParGroupe(
            @PathVariable UUID groupeId) {
        List<TypeCotisationDto> types = typeCotisationService.obtenirTypesCotisationParGroupe(groupeId);
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    @GetMapping("/groupe/{groupeId}/actifs")
    @Operation(summary = "Lister les types de cotisation actifs d'un groupe")
    public ResponseEntity<ApiResponse<List<TypeCotisationDto>>> obtenirTypesActifsParGroupe(
            @PathVariable UUID groupeId) {
        List<TypeCotisationDto> types = typeCotisationService.obtenirTypesCotisationActifs(groupeId);
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un type de cotisation par ID")
    public ResponseEntity<ApiResponse<TypeCotisationDto>> obtenirParId(@PathVariable UUID id) {
        TypeCotisationDto dto = typeCotisationService.obtenirParId(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PutMapping("/{id}/desactiver")
    @Operation(summary = "Désactiver un type de cotisation")
    public ResponseEntity<ApiResponse<TypeCotisationDto>> desactiver(@PathVariable UUID id) {
        TypeCotisationDto dto = typeCotisationService.desactiver(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "Type de cotisation désactivé"));
    }
}
