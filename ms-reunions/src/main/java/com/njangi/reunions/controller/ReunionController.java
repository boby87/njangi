package com.njangi.reunions.controller;

import com.njangi.reunions.dto.CreateReunionRequest;
import com.njangi.reunions.dto.ReunionDto;
import com.njangi.reunions.service.ReunionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reunions")
@RequiredArgsConstructor
public class ReunionController {

    private final ReunionService reunionService;

    @GetMapping
    public ResponseEntity<List<ReunionDto>> findAll() {
        return ResponseEntity.ok(reunionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReunionDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(reunionService.findById(id));
    }

    @GetMapping("/groupe/{groupeId}")
    public ResponseEntity<List<ReunionDto>> findByGroupe(@PathVariable UUID groupeId) {
        return ResponseEntity.ok(reunionService.findByGroupe(groupeId));
    }

    @PostMapping
    public ResponseEntity<ReunionDto> create(@Valid @RequestBody CreateReunionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reunionService.create(request));
    }

    @PutMapping("/{id}/terminer")
    public ResponseEntity<ReunionDto> terminer(
            @PathVariable UUID id,
            @RequestParam(required = false) String compteRendu) {
        return ResponseEntity.ok(reunionService.terminerReunion(id, compteRendu));
    }
}
