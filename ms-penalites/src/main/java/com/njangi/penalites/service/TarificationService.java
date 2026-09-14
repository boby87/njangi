package com.njangi.penalites.service;

import com.njangi.penalites.dto.DefinirTarifRequest;
import com.njangi.penalites.dto.TarificationPenaliteDto;
import com.njangi.penalites.entity.TarificationPenalite;
import com.njangi.penalites.exception.NotFoundException;
import com.njangi.penalites.repository.TarificationPenaliteRepository;
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
            log.info("Mise a jour du tarif : groupe={}, type={}, montant={}",
                    request.groupeId(), request.typeInfraction(), request.montant());
        } else {
            entity = new TarificationPenalite(
                    null,
                    request.groupeId(),
                    request.typeInfraction(),
                    request.montant(),
                    request.actif()
            );
            log.info("Creation du tarif : groupe={}, type={}, montant={}",
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
                .orElseThrow(() -> new NotFoundException("Tarif non trouve : " + id));
        return toDto(entity);
    }

    public TarificationPenaliteDto toDto(TarificationPenalite entity) {
        return TarificationPenaliteDto.from(entity);
    }
}
