package com.njangi.statistiques.controller;

import com.njangi.statistiques.dto.StatistiqueGroupeDto;
import com.njangi.statistiques.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/statistiques")
@RequiredArgsConstructor
public class StatistiqueController {

    private final StatistiqueService statistiqueService;

    @GetMapping("/groupe/{groupeId}")
    public ResponseEntity<List<StatistiqueGroupeDto>> findByGroupe(@PathVariable UUID groupeId) {
        return ResponseEntity.ok(statistiqueService.findByGroupe(groupeId));
    }

    @GetMapping("/groupe/{groupeId}/session/{sessionId}")
    public ResponseEntity<StatistiqueGroupeDto> findByGroupeAndSession(
            @PathVariable UUID groupeId,
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(statistiqueService.findByGroupeAndSession(groupeId, sessionId));
    }

    /**
     * Déclenche un recalcul forcé des statistiques pour un groupe/session.
     * À réserver aux administrateurs ou aux jobs de réconciliation.
     */
    @PostMapping("/groupe/{groupeId}/session/{sessionId}/calculer")
    public ResponseEntity<StatistiqueGroupeDto> calculer(
            @PathVariable UUID groupeId,
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(statistiqueService.calculer(groupeId, sessionId));
    }
}
