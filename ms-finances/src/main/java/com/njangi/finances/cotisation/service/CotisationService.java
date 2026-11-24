package com.njangi.finances.cotisation.service;

import com.njangi.finances.cotisation.dto.CotisationDto;
import com.njangi.finances.cotisation.dto.CreateCotisationRequest;
import com.njangi.finances.cotisation.dto.GenererCotisationsReunionRequest;
import com.njangi.finances.cotisation.dto.MettreAJourPaiementCotisationRequest;
import com.njangi.finances.cotisation.entity.Cotisation;
import com.njangi.finances.cotisation.entity.StatutCotisation;
import com.njangi.finances.cotisation.entity.TypeCotisation;
import com.njangi.finances.cotisation.repository.CotisationRepository;
import com.njangi.finances.cotisation.repository.TypeCotisationRepository;
import com.njangi.finances.event.FinancesEventPublisher;
import com.njangi.finances.exception.NotFoundException;
import com.njangi.finances.penalite.service.PenaliteService;
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
    private final PenaliteService penaliteService;
    private final FinancesEventPublisher eventPublisher;

    public CotisationService(CotisationRepository cotisationRepository,
                             TypeCotisationRepository typeCotisationRepository,
                             PenaliteService penaliteService,
                             FinancesEventPublisher eventPublisher) {
        this.cotisationRepository = cotisationRepository;
        this.typeCotisationRepository = typeCotisationRepository;
        this.penaliteService = penaliteService;
        this.eventPublisher = eventPublisher;
    }

    public CotisationDto creerCotisation(CreateCotisationRequest request) {
        TypeCotisation type = typeCotisationRepository.findById(request.typeCotisationId())
                .orElseThrow(() -> new NotFoundException("Type de cotisation non trouvé : " + request.typeCotisationId()));

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
        eventPublisher.publierCotisationCreee(saved.getId(), saved.getMembreId(), saved.getGroupeId(),
                saved.getReunionId(), saved.getMontantDu());
        log.info("Cotisation créée : id={}, membreId={}, montant={}", saved.getId(), saved.getMembreId(), saved.getMontantDu());
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
                    eventPublisher.publierCotisationCreee(saved.getId(), membreId, request.groupeId(),
                            request.reunionId(), type.getMontant());
                }
            }
        }

        log.info("{} cotisations générées pour la réunion {} du groupe {}",
                cotisationsCreees.size(), request.reunionId(), request.groupeId());
        return cotisationsCreees.stream().map(this::toDto).toList();
    }

    public CotisationDto enregistrerPaiement(UUID cotisationId, MettreAJourPaiementCotisationRequest request) {
        Cotisation cotisation = cotisationRepository.findById(cotisationId)
                .orElseThrow(() -> new NotFoundException("Cotisation non trouvée : " + cotisationId));
        return appliquerPaiementSurEntite(cotisation, request.montantVerse());
    }

    /**
     * Met à jour atomiquement la cotisation dans la même transaction ACID que la validation du paiement.
     * Si la cotisation n'existe pas en base, retourne Optional.empty() sans bloquer la validation du paiement.
     */
    public Optional<CotisationDto> appliquerPaiementSiExistante(UUID cotisationId, BigDecimal montantVerse) {
        if (cotisationId == null || montantVerse == null) {
            return Optional.empty();
        }
        Optional<Cotisation> cotisationOpt = cotisationRepository.findById(cotisationId);
        if (cotisationOpt.isEmpty()) {
            log.debug("Aucune cotisation trouvée en base pour cotisationId={} lors de la validation du paiement", cotisationId);
            return Optional.empty();
        }
        return Optional.of(appliquerPaiementSurEntite(cotisationOpt.get(), montantVerse));
    }

    private CotisationDto appliquerPaiementSurEntite(Cotisation cotisation, BigDecimal montantVerse) {
        BigDecimal nouveauMontantPaye = cotisation.getMontantPaye().add(montantVerse);
        cotisation.setMontantPaye(nouveauMontantPaye);

        boolean soldee = false;
        if (nouveauMontantPaye.compareTo(cotisation.getMontantDu()) >= 0) {
            cotisation.setStatut(StatutCotisation.PAYE);
            cotisation.setDatePaiement(LocalDateTime.now());
            soldee = true;
        } else if (nouveauMontantPaye.compareTo(BigDecimal.ZERO) > 0) {
            cotisation.setStatut(StatutCotisation.PARTIEL);
        }

        Cotisation updated = cotisationRepository.save(cotisation);
        if (soldee) {
            eventPublisher.publierCotisationPayee(updated.getId(), updated.getMembreId(),
                    updated.getGroupeId(), updated.getReunionId(), nouveauMontantPaye);
        }

        log.info("Paiement enregistré pour cotisation {} : montantVerse={}, montantPayeTotal={}, statut={}",
                updated.getId(), montantVerse, nouveauMontantPaye, updated.getStatut());
        return toDto(updated);
    }

    public CotisationDto marquerEnRetard(UUID cotisationId) {
        Cotisation cotisation = cotisationRepository.findById(cotisationId)
                .orElseThrow(() -> new NotFoundException("Cotisation non trouvée : " + cotisationId));

        if (cotisation.getStatut() != StatutCotisation.PAYE) {
            cotisation.setStatut(StatutCotisation.EN_RETARD);
            cotisationRepository.save(cotisation);
            eventPublisher.publierCotisationRetard(cotisation.getId(), cotisation.getMembreId(),
                    cotisation.getGroupeId(), cotisation.getReunionId(), cotisation.getMontantDu());

            boolean penalisee = penaliteService.appliquerPenaliteRetardSiTarifConfigure(
                    cotisation.getMembreId(),
                    cotisation.getGroupeId(),
                    null,
                    cotisation.getReunionId(),
                    cotisation.getId()
            ).isPresent();

            if (penalisee) {
                cotisation.setStatut(StatutCotisation.PENALISE);
                cotisationRepository.save(cotisation);
            }
            log.info("Cotisation {} marquée en retard (statut final={})", cotisationId, cotisation.getStatut());
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
                .orElseThrow(() -> new NotFoundException("Cotisation non trouvée : " + id));
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
