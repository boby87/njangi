package com.njangi.finances.penalite.controller;

import com.njangi.finances.common.ApiResponse;
import com.njangi.finances.penalite.dto.DefinirTarifRequest;
import com.njangi.finances.penalite.dto.TarificationPenaliteDto;
import com.njangi.finances.penalite.service.TarificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/penalites/tarifs")
@Tag(name = "Barème Disciplinaire", description = "Paramétrage de la tarification des infractions par groupe (retards, absences, bavardages)")
public class TarificationController {

    private final TarificationService tarificationService;

    public TarificationController(TarificationService tarificationService) {
        this.tarificationService = tarificationService;
    }

    @PostMapping
    @Operation(summary = "Définir ou mettre à jour un tarif d'infraction pour un groupe")
    public ResponseEntity<ApiResponse<TarificationPenaliteDto>> definirTarif(
            @Valid @RequestBody DefinirTarifRequest request) {
        TarificationPenaliteDto dto = tarificationService.definirOuMettreAJourTarif(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Tarif d'infraction enregistré avec succès"));
    }

    @GetMapping("/groupe/{groupeId}")
    @Operation(summary = "Lister l'ensemble des tarifs d'infraction configurés pour un groupe")
    public ResponseEntity<ApiResponse<List<TarificationPenaliteDto>>> obtenirTarifsParGroupe(
            @PathVariable UUID groupeId) {
        List<TarificationPenaliteDto> dtos = tarificationService.obtenirTarifsParGroupe(groupeId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/groupe/{groupeId}/actifs")
    @Operation(summary = "Lister les tarifs d'infraction actifs d'un groupe")
    public ResponseEntity<ApiResponse<List<TarificationPenaliteDto>>> obtenirTarifsActifsParGroupe(
            @PathVariable UUID groupeId) {
        List<TarificationPenaliteDto> dtos = tarificationService.obtenirTarifsActifsParGroupe(groupeId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'un tarif par son identifiant")
    public ResponseEntity<ApiResponse<TarificationPenaliteDto>> obtenirParId(@PathVariable UUID id) {
        TarificationPenaliteDto dto = tarificationService.obtenirParId(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
