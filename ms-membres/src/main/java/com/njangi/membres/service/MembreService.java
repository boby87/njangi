package com.njangi.membres.service;

import com.njangi.membres.dto.*;
import com.njangi.membres.entity.Membre;
import com.njangi.membres.exception.BusinessException;
import com.njangi.membres.exception.NotFoundException;
import com.njangi.membres.repository.MembreRepository;
import com.njangi.membres.event.MembreEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MembreService {

    private final MembreRepository membreRepository;
    private final MembreEventPublisher eventPublisher;

    public List<MembreDto> findAll() {
        return membreRepository.findAll().stream().map(this::toDto).toList();
    }

    public MembreDto findById(UUID id) {
        return membreRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Membre introuvable : " + id));
    }

    @Transactional
    public MembreDto create(CreateMembreRequest request) {
        if (membreRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email deja utilise : " + request.email());
        }
        if (membreRepository.existsByTelephone(request.telephone())) {
            throw new BusinessException("Telephone deja utilise : " + request.telephone());
        }

        Membre membre = Membre.builder()
                .authUtilisateurId(request.authUtilisateurId())
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .telephone(request.telephone())
                .dateNaissance(request.dateNaissance())
                .adresse(request.adresse())
                .ville(request.ville())
                .build();

        Membre saved = membreRepository.save(membre);
        eventPublisher.publishMembreInscrit(saved);
        log.info("Membre cree : {}", saved.getId());
        return toDto(saved);
    }

    private MembreDto toDto(Membre m) {
        return new MembreDto(m.getId(), m.getAuthUtilisateurId(), m.getNom(), m.getPrenom(),
                m.getEmail(), m.getTelephone(), m.getDateNaissance(), m.getAdresse(),
                m.getVille(), m.getPhotoUrl(), m.getStatut(), m.getCreatedAt());
    }
}
