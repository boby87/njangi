package com.njangi.finances.cotisation.service;

import com.njangi.finances.cotisation.dto.CreerTypeCotisationRequest;
import com.njangi.finances.cotisation.dto.TypeCotisationDto;
import com.njangi.finances.cotisation.entity.TypeCotisation;
import com.njangi.finances.cotisation.repository.TypeCotisationRepository;
import com.njangi.finances.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TypeCotisationService {

    private static final Logger log = LoggerFactory.getLogger(TypeCotisationService.class);
    private final TypeCotisationRepository typeCotisationRepository;

    public TypeCotisationService(TypeCotisationRepository typeCotisationRepository) {
        this.typeCotisationRepository = typeCotisationRepository;
    }

    public TypeCotisationDto creerTypeCotisation(CreerTypeCotisationRequest request) {
        TypeCotisation entity = new TypeCotisation(
                null,
                request.groupeId(),
                request.libelle(),
                request.description(),
                request.categorie(),
                request.montant(),
                request.estRotatif(),
                request.estObligatoire(),
                "ACTIF"
        );

        TypeCotisation saved = typeCotisationRepository.save(entity);
        log.info("Type de cotisation créé : id={}, groupeId={}, libelle='{}', montant={}",
                saved.getId(), saved.getGroupeId(), saved.getLibelle(), saved.getMontant());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TypeCotisationDto> obtenirTypesCotisationParGroupe(UUID groupeId) {
        return typeCotisationRepository.findByGroupeId(groupeId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TypeCotisationDto> obtenirTypesCotisationActifs(UUID groupeId) {
        return typeCotisationRepository.findByGroupeIdAndStatut(groupeId, "ACTIF")
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TypeCotisationDto obtenirParId(UUID id) {
        TypeCotisation entity = typeCotisationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Type de cotisation non trouvé : " + id));
        return toDto(entity);
    }

    public TypeCotisationDto desactiver(UUID id) {
        TypeCotisation entity = typeCotisationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Type de cotisation non trouvé : " + id));
        entity.setStatut("INACTIF");
        TypeCotisation updated = typeCotisationRepository.save(entity);
        log.info("Type de cotisation désactivé : id={}", id);
        return toDto(updated);
    }

    public TypeCotisationDto toDto(TypeCotisation entity) {
        return new TypeCotisationDto(
                entity.getId(),
                entity.getGroupeId(),
                entity.getLibelle(),
                entity.getDescription(),
                entity.getCategorie(),
                entity.getMontant(),
                entity.isEstRotatif(),
                entity.isEstObligatoire(),
                entity.getStatut(),
                entity.getCreatedAt()
        );
    }
}
