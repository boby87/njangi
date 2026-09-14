package com.njangi.statistiques.controller;

import com.njangi.statistiques.dto.ApiResponse;
import com.njangi.statistiques.dto.BilanFinancierDto;
import com.njangi.statistiques.dto.StatistiqueGroupeDto;
import com.njangi.statistiques.service.StatistiqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/statistiques")
@Tag(name = "Statistiques & Reporting", description = "API de reporting, bilans de trésorerie et indicateurs de performance Njangi")
public class StatistiqueController {

    private final StatistiqueService statistiqueService;

    public StatistiqueController(StatistiqueService statistiqueService) {
        this.statistiqueService = statistiqueService;
    }

    @GetMapping("/groupe/{groupeId}")
    @Operation(summary = "Historique des statistiques d'un groupe", description = "Retourne la liste des agrégats pour l'ensemble des sessions d'un groupe")
    public ResponseEntity<ApiResponse<List<StatistiqueGroupeDto>>> findByGroupe(@PathVariable UUID groupeId) {
        List<StatistiqueGroupeDto> stats = statistiqueService.findByGroupe(groupeId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/groupe/{groupeId}/session/{sessionId}")
    @Operation(summary = "Statistiques d'une session", description = "Retourne les agrégats financiers et de présence pour une session précise")
    public ResponseEntity<ApiResponse<StatistiqueGroupeDto>> findByGroupeAndSession(
            @PathVariable UUID groupeId,
            @PathVariable UUID sessionId) {
        StatistiqueGroupeDto stat = statistiqueService.findByGroupeAndSession(groupeId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(stat));
    }

    @PostMapping("/groupe/{groupeId}/session/{sessionId}/calculer")
    @Operation(summary = "Recalculer les statistiques d'une session", description = "Force le recalcul et la synchronisation des agrégats pour une session")
    public ResponseEntity<ApiResponse<StatistiqueGroupeDto>> calculer(
            @PathVariable UUID groupeId,
            @PathVariable UUID sessionId) {
        StatistiqueGroupeDto stat = statistiqueService.calculer(groupeId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(stat, "Statistiques recalculées avec succès"));
    }

    @GetMapping("/groupe/{groupeId}/session/{sessionId}/bilan")
    @Operation(summary = "Bilan financier consolidé", description = "Génère un bilan complet de trésorerie : caisse cash vs mobile money, décaissements, pénalités et ratio de recouvrement")
    public ResponseEntity<ApiResponse<BilanFinancierDto>> genererBilan(
            @PathVariable UUID groupeId,
            @PathVariable UUID sessionId) {
        BilanFinancierDto bilan = statistiqueService.genererBilanFinancier(groupeId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(bilan, "Bilan financier généré avec succès"));
    }
}
