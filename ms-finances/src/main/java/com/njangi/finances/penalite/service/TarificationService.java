package com.njangi.finances.penalite.service;

import com.njangi.finances.exception.NotFoundException;
import com.njangi.finances.penalite.dto.DefinirTarifRequest;
import com.njangi.finances.penalite.dto.TarificationPenaliteDto;
import com.njangi.finances.penalite.entity.TarificationPenalite;
import com.njangi.finances.penalite.repository.TarificationPenaliteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TarificationService {

    private static final Logger log = LoggerFactory.getLogger(TarificationService.class);
    private final TarificationPenaliteRepository tarificationRepository;

    public TarificationService(TarificationPenaliteRepository tarificationRepository) {
        this.tarificationRepository = tarificationRepository;
    }

    public TarificationPenaliteDto definirOuMettreAJourTarif(DefinirTarifRequest request) {
        Optional<TarificationPenalite> existant = tarificationRepository
                .findByGroupeIdAndTypeInfraction(request.groupeId(), request.typeInfraction());

        TarificationPenalite entity;
        if (existant.isPresent()) {
            entity = existant.get();
            entity.setMontant(request.montant());
            entity.setActif(request.actif());
            log.info("Mise à jour du tarif : groupe={}, type={}, montant={}",
                    request.groupeId(), request.typeInfraction(), request.montant());
        } else {
            entity = new TarificationPenalite(
                    null,
                    request.groupeId(),
                    request.typeInfraction(),
                    request.montant(),
                    request.actif()
            );
            log.info("Création du tarif : groupe={}, type={}, montant={}",
                    request.groupeId(), request.typeInfraction(), request.montant());
        }

        TarificationPenalite saved = tarificationRepository.save(entity);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TarificationPenaliteDto> obtenirTarifsParGroupe(UUID groupeId) {
        return tarificationRepository.findByGroupeId(groupeId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TarificationPenaliteDto> obtenirTarifsActifsParGroupe(UUID groupeId) {
        return tarificationRepository.findByGroupeIdAndActifTrue(groupeId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TarificationPenaliteDto obtenirParId(UUID id) {
        TarificationPenalite entity = tarificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarif non trouvé : " + id));
        return toDto(entity);
    }

    public TarificationPenaliteDto toDto(TarificationPenalite entity) {
        return TarificationPenaliteDto.from(entity);
    }
}
