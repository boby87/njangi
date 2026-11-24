package com.njangi.finances.event;

import com.njangi.finances.config.KafkaConfig;
import com.njangi.finances.cotisation.entity.ModeVersement;
import com.njangi.finances.paiement.entity.ModePaiement;
import com.njangi.finances.penalite.entity.Penalite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class FinancesEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(FinancesEventPublisher.class);

    public static final String EVENT_COTISATION_CREEE = "COTISATION_CREEE";
    public static final String EVENT_COTISATION_PAYEE = "COTISATION_PAYEE";
    public static final String EVENT_COTISATION_RETARD = "COTISATION_RETARD";
    public static final String EVENT_PAIEMENT_INITIE = "PAIEMENT_INITIE";
    public static final String EVENT_PAIEMENT_VALIDE = "PAIEMENT_VALIDE";
    public static final String EVENT_PAIEMENT_REJETE = "PAIEMENT_REJETE";
    public static final String EVENT_POT_ATTRIBUE = "POT_ATTRIBUE";
    public static final String EVENT_POT_VERSE = "POT_VERSE";
    public static final String EVENT_SANCTION_INFLIGEE = "SANCTION_INFLIGEE";
    public static final String EVENT_SANCTION_PAYEE = "SANCTION_PAYEE";
    public static final String EVENT_SANCTION_ANNULEE = "SANCTION_ANNULEE";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FinancesEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publierCotisationCreee(UUID cotisationId, UUID membreId, UUID groupeId,
                                       UUID reunionId, BigDecimal montant) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("cotisationId", cotisationId.toString());
        payload.put("membreId", membreId.toString());
        if (reunionId != null) {
            payload.put("reunionId", reunionId.toString());
        }
        payload.put("montant", montant);
        send(cotisationId.toString(), DomainEvent.of(EVENT_COTISATION_CREEE, groupeId, membreId, Collections.unmodifiableMap(payload)));
    }

    public void publierCotisationPayee(UUID cotisationId, UUID membreId, UUID groupeId,
                                       UUID reunionId, BigDecimal montant) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("cotisationId", cotisationId.toString());
        payload.put("membreId", membreId.toString());
        if (reunionId != null) {
            payload.put("reunionId", reunionId.toString());
        }
        payload.put("montant", montant);
        send(cotisationId.toString(), DomainEvent.of(EVENT_COTISATION_PAYEE, groupeId, membreId, Collections.unmodifiableMap(payload)));
    }

    public void publierCotisationRetard(UUID cotisationId, UUID membreId, UUID groupeId,
                                        UUID reunionId, BigDecimal montantDu) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("cotisationId", cotisationId.toString());
        payload.put("membreId", membreId.toString());
        if (reunionId != null) {
            payload.put("reunionId", reunionId.toString());
        }
        payload.put("montant", montantDu);
        send(cotisationId.toString(), DomainEvent.of(EVENT_COTISATION_RETARD, groupeId, membreId, Collections.unmodifiableMap(payload)));
    }

    public void publierPaiementInitie(UUID paiementId, UUID cotisationId, UUID membreId,
                                      UUID groupeId, BigDecimal montant, ModePaiement modePaiement, String reference) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paiementId", paiementId.toString());
        if (cotisationId != null) {
            payload.put("cotisationId", cotisationId.toString());
        }
        payload.put("membreId", membreId.toString());
        payload.put("montant", montant);
        payload.put("modePaiement", modePaiement.name());
        payload.put("reference", reference != null ? reference : "");
        send(paiementId.toString(), DomainEvent.of(EVENT_PAIEMENT_INITIE, groupeId, membreId, Collections.unmodifiableMap(payload)));
    }

    public void publierPaiementValide(UUID paiementId, UUID cotisationId, UUID membreId,
                                      UUID groupeId, BigDecimal montant, ModePaiement modePaiement, String reference) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paiementId", paiementId.toString());
        if (cotisationId != null) {
            payload.put("cotisationId", cotisationId.toString());
        }
        payload.put("membreId", membreId.toString());
        payload.put("montant", montant);
        payload.put("modePaiement", modePaiement.name());
        payload.put("reference", reference != null ? reference : "");
        send(paiementId.toString(), DomainEvent.of(EVENT_PAIEMENT_VALIDE, groupeId, membreId, Collections.unmodifiableMap(payload)));
    }

    public void publierPaiementRejete(UUID paiementId, UUID cotisationId, UUID membreId,
                                      UUID groupeId, ModePaiement modePaiement, String statut, String motif) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("paiementId", paiementId.toString());
        if (cotisationId != null) {
            payload.put("cotisationId", cotisationId.toString());
        }
        payload.put("membreId", membreId.toString());
        if (modePaiement != null) {
            payload.put("modePaiement", modePaiement.name());
        }
        if (statut != null) {
            payload.put("statut", statut);
        }
        payload.put("motif", motif != null ? motif : "");
        send(paiementId.toString(), DomainEvent.of(EVENT_PAIEMENT_REJETE, groupeId, membreId, Collections.unmodifiableMap(payload)));
    }

    public void publierPotAttribue(UUID potSessionId, UUID groupeId, UUID sessionId, UUID reunionId,
                                   UUID membreBeneficiaireId, BigDecimal montantTotal, BigDecimal montantNet) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("potSessionId", potSessionId.toString());
        if (sessionId != null) {
            payload.put("sessionId", sessionId.toString());
        }
        if (reunionId != null) {
            payload.put("reunionId", reunionId.toString());
        }
        payload.put("membreId", membreBeneficiaireId.toString());
        payload.put("membreBeneficiaireId", membreBeneficiaireId.toString());
        payload.put("montant", montantNet != null ? montantNet : montantTotal);
        payload.put("montantTotal", montantTotal);
        if (montantNet != null) {
            payload.put("montantNet", montantNet);
        }
        send(potSessionId.toString(), DomainEvent.of(EVENT_POT_ATTRIBUE, groupeId, membreBeneficiaireId, Collections.unmodifiableMap(payload)));
    }

    public void publierPotVerse(UUID potSessionId, UUID groupeId, UUID sessionId, UUID reunionId,
                                UUID membreBeneficiaireId, BigDecimal montantNet,
                                ModeVersement modeVersement, String referencePaiement) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("potSessionId", potSessionId.toString());
        if (sessionId != null) {
            payload.put("sessionId", sessionId.toString());
        }
        if (reunionId != null) {
            payload.put("reunionId", reunionId.toString());
        }
        payload.put("membreId", membreBeneficiaireId.toString());
        payload.put("membreBeneficiaireId", membreBeneficiaireId.toString());
        payload.put("montant", montantNet);
        payload.put("montantNet", montantNet);
        if (modeVersement != null) {
            payload.put("modePaiement", modeVersement.name());
            payload.put("modeVersement", modeVersement.name());
        }
        if (referencePaiement != null) {
            payload.put("referencePaiement", referencePaiement);
        }
        send(potSessionId.toString(), DomainEvent.of(EVENT_POT_VERSE, groupeId, membreBeneficiaireId, Collections.unmodifiableMap(payload)));
    }

    public void publierSanctionInfligee(Penalite penalite) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("penaliteId", penalite.getId().toString());
        payload.put("membreId", penalite.getMembreId().toString());
        if (penalite.getSessionId() != null) {
            payload.put("sessionId", penalite.getSessionId().toString());
        }
        if (penalite.getReunionId() != null) {
            payload.put("reunionId", penalite.getReunionId().toString());
        }
        payload.put("typeInfraction", penalite.getTypeInfraction().name());
        payload.put("montant", penalite.getMontant());
        payload.put("statut", penalite.getStatut().name());
        if (penalite.getMotif() != null) {
            payload.put("motif", penalite.getMotif());
        }
        send(penalite.getId().toString(), DomainEvent.of(EVENT_SANCTION_INFLIGEE, penalite.getGroupeId(), penalite.getMembreId(), Collections.unmodifiableMap(payload)));
    }

    public void publierSanctionPayee(Penalite penalite) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("penaliteId", penalite.getId().toString());
        payload.put("membreId", penalite.getMembreId().toString());
        if (penalite.getSessionId() != null) {
            payload.put("sessionId", penalite.getSessionId().toString());
        }
        payload.put("typeInfraction", penalite.getTypeInfraction().name());
        payload.put("montant", penalite.getMontant());
        if (penalite.getDatePaiement() != null) {
            payload.put("datePaiement", penalite.getDatePaiement().toString());
        }
        send(penalite.getId().toString(), DomainEvent.of(EVENT_SANCTION_PAYEE, penalite.getGroupeId(), penalite.getMembreId(), Collections.unmodifiableMap(payload)));
    }

    public void publierSanctionAnnulee(Penalite penalite) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("penaliteId", penalite.getId().toString());
        payload.put("membreId", penalite.getMembreId().toString());
        if (penalite.getSessionId() != null) {
            payload.put("sessionId", penalite.getSessionId().toString());
        }
        payload.put("typeInfraction", penalite.getTypeInfraction().name());
        payload.put("montant", penalite.getMontant());
        payload.put("motif", penalite.getMotif() != null ? penalite.getMotif() : "");
        send(penalite.getId().toString(), DomainEvent.of(EVENT_SANCTION_ANNULEE, penalite.getGroupeId(), penalite.getMembreId(), Collections.unmodifiableMap(payload)));
    }

    private void send(String key, DomainEvent event) {
        kafkaTemplate.send(KafkaConfig.TOPIC_FINANCES_EVENTS, key, event);
        log.info("Événement {} publié sur {} : eventId={}, groupeId={}, key={}",
                event.eventType(), KafkaConfig.TOPIC_FINANCES_EVENTS, event.eventId(), event.groupeId(), key);
    }
}
