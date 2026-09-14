package com.njangi.cotisations.service;

import com.njangi.cotisations.dto.CotisationDto;
import com.njangi.cotisations.dto.CreateCotisationRequest;
import com.njangi.cotisations.dto.GenererCotisationsReunionRequest;
import com.njangi.cotisations.dto.MettreAJourPaiementCotisationRequest;
import com.njangi.cotisations.entity.Cotisation;
import com.njangi.cotisations.entity.StatutCotisation;
import com.njangi.cotisations.entity.TypeCotisation;
import com.njangi.cotisations.event.CotisationEventPublisher;
import com.njangi.cotisations.exception.NotFoundException;
import com.njangi.cotisations.repository.CotisationRepository;
import com.njangi.cotisations.repository.TypeCotisationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CotisationService {

    private static final Logger log = LoggerFactory.getLogger(CotisationService.class);

    private final CotisationRepository cotisationRepository;
    private final TypeCotisationRepository typeCotisationRepository;
    private final CotisationEventPublisher eventPublisher;

    public CotisationService(CotisationRepository cotisationRepository,
                             TypeCotisationRepository typeCotisationRepository,
                             CotisationEventPublisher eventPublisher) {
        this.cotisationRepository = cotisationRepository;
        this.typeCotisationRepository = typeCotisationRepository;
        this.eventPublisher = eventPublisher;
    }

    public CotisationDto creerCotisation(CreateCotisationRequest request) {
        TypeCotisation type = typeCotisationRepository.findById(request.typeCotisationId())
                .orElseThrow(() -> new NotFoundException("Type de cotisation non trouve : " + request.typeCotisationId()));

        Cotisation cotisation = new Cotisation(
                null,
                request.groupeId(),
                request.membreId(),
                type.getId(),
                request.reunionId() != null ? request.reunionId() : UUID.randomUUID(),
                request.montantDu(),
                BigDecimal.ZERO,
                StatutCotisation.EN_ATTENTE,
                request.dateLimitePaiement()
        );

        Cotisation saved = cotisationRepository.save(cotisation);
        eventPublisher.publierCotisationGeneree(saved.getId(), saved.getMembreId(), saved.getGroupeId(), saved.getMontantDu());
        log.info("Cotisation creee : id={}, membreId={}, montant={}", saved.getId(), saved.getMembreId(), saved.getMontantDu());
        return toDto(saved);
    }

    public List<CotisationDto> genererCotisationsPourReunion(GenererCotisationsReunionRequest request) {
        List<TypeCotisation> typesObligatoires = typeCotisationRepository
                .findByGroupeIdAndEstObligatoireTrueAndStatut(request.groupeId(), "ACTIF");

        List<Cotisation> cotisationsCreees = new ArrayList<>();

        for (UUID membreId : request.membreIds()) {
            for (TypeCotisation type : typesObligatoires) {
                Optional<Cotisation> existante = cotisationRepository
                        .findByReunionIdAndMembreIdAndTypeCotisationId(request.reunionId(), membreId, type.getId());

                if (existante.isEmpty()) {
                    Cotisation cotisation = new Cotisation(
                            null,
                            request.groupeId(),
                            membreId,
                            type.getId(),
                            request.reunionId(),
                            type.getMontant(),
                            BigDecimal.ZERO,
                            StatutCotisation.EN_ATTENTE,
                            request.dateLimitePaiement()
                    );
                    Cotisation saved = cotisationRepository.save(cotisation);
                    cotisationsCreees.add(saved);
                    eventPublisher.publierCotisationGeneree(saved.getId(), membreId, request.groupeId(), type.getMontant());
                }
            }
        }

        log.info("{} cotisations generees pour la reunion {} du groupe {}",
                cotisationsCreees.size(), request.reunionId(), request.groupeId());
        return cotisationsCreees.stream().map(this::toDto).toList();
    }

    public CotisationDto enregistrerPaiement(UUID cotisationId, MettreAJourPaiementCotisationRequest request) {
        Cotisation cotisation = cotisationRepository.findById(cotisationId)
                .orElseThrow(() -> new NotFoundException("Cotisation non trouvee : " + cotisationId));

        BigDecimal nouveauMontantPaye = cotisation.getMontantPaye().add(request.montantVerse());
        cotisation.setMontantPaye(nouveauMontantPaye);

        if (nouveauMontantPaye.compareTo(cotisation.getMontantDu()) >= 0) {
            cotisation.setStatut(StatutCotisation.PAYE);
            cotisation.setDatePaiement(LocalDateTime.now());
            eventPublisher.publierCotisationPayee(cotisation.getId(), cotisation.getMembreId(),
                    cotisation.getGroupeId(), nouveauMontantPaye);
        } else if (nouveauMontantPaye.compareTo(BigDecimal.ZERO) > 0) {
            cotisation.setStatut(StatutCotisation.PARTIEL);
        }

        Cotisation updated = cotisationRepository.save(cotisation);
        log.info("Paiement enregistre pour cotisation {} : montantVerse={}, montantPayeTotal={}, statut={}",
                cotisationId, request.montantVerse(), nouveauMontantPaye, updated.getStatut());
        return toDto(updated);
    }

    public CotisationDto marquerEnRetard(UUID cotisationId) {
        Cotisation cotisation = cotisationRepository.findById(cotisationId)
                .orElseThrow(() -> new NotFoundException("Cotisation non trouvee : " + cotisationId));

        if (cotisation.getStatut() != StatutCotisation.PAYE) {
            cotisation.setStatut(StatutCotisation.EN_RETARD);
            cotisationRepository.save(cotisation);
            log.info("Cotisation {} marquee en retard", cotisationId);
        }
        return toDto(cotisation);
    }

    @Transactional(readOnly = true)
    public List<CotisationDto> obtenirCotisationsParReunion(UUID reunionId) {
        return cotisationRepository.findByReunionId(reunionId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CotisationDto> obtenirCotisationsParMembre(UUID membreId) {
        return cotisationRepository.findByMembreId(membreId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CotisationDto> obtenirCotisationsParGroupe(UUID groupeId) {
        return cotisationRepository.findByGroupeId(groupeId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CotisationDto obtenirParId(UUID id) {
        Cotisation entity = cotisationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cotisation non trouvee : " + id));
        return toDto(entity);
    }

    public CotisationDto toDto(Cotisation c) {
        return new CotisationDto(
                c.getId(),
                c.getGroupeId(),
                c.getMembreId(),
                c.getTypeCotisationId(),
                c.getReunionId(),
                c.getMontantDu(),
                c.getMontantPaye(),
                c.getStatut(),
                c.getDateLimitePaiement(),
                c.getDatePaiement(),
                c.getCreatedAt()
        );
    }
}
