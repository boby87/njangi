package com.njangi.groupes.controller;

import com.njangi.groupes.dto.*;
import com.njangi.groupes.service.GroupeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groupes")
@RequiredArgsConstructor
public class GroupeController {

    private final GroupeService groupeService;

    @GetMapping
    public ResponseEntity<List<GroupeDto>> getAll() {
        return ResponseEntity.ok(groupeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupeDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(groupeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<GroupeDto> create(@Valid @RequestBody CreateGroupeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupeService.create(request));
    }

    @PostMapping("/{id}/membres")
    public ResponseEntity<GroupeMembreDto> ajouterMembre(
            @PathVariable UUID id,
            @Valid @RequestBody AjouterMembreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupeService.ajouterMembre(id, request));
    }

    @GetMapping("/{id}/membres")
    public ResponseEntity<List<GroupeMembreDto>> getMembres(@PathVariable UUID id) {
        return ResponseEntity.ok(groupeService.getMembres(id));
    }
}
