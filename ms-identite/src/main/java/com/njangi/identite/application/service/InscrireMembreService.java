package com.njangi.identite.application.service;

import com.njangi.identite.domain.event.MembreInscritEvent;
import com.njangi.identite.domain.exception.BusinessException;
import com.njangi.identite.domain.model.Email;
import com.njangi.identite.domain.model.Membre;
import com.njangi.identite.domain.model.MembreId;
import com.njangi.identite.domain.model.StatutMembre;
import com.njangi.identite.domain.model.Telephone;
import com.njangi.identite.domain.port.in.InscrireMembreUseCase;
import com.njangi.identite.domain.port.out.IdentiteEventPublisherPort;
import com.njangi.identite.domain.port.out.MembreRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class InscrireMembreService implements InscrireMembreUseCase {

    private final MembreRepositoryPort membreRepositoryPort;
    private final IdentiteEventPublisherPort eventPublisherPort;

    public InscrireMembreService(
            MembreRepositoryPort membreRepositoryPort,
            IdentiteEventPublisherPort eventPublisherPort
    ) {
        this.membreRepositoryPort = membreRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    @Transactional
    public Membre inscrire(
            UUID authUtilisateurId,
            String nom,
            String prenom,
            String emailStr,
            String telephoneStr,
            LocalDate dateNaissance,
            String adresse,
            String ville
    ) {
        Email email = new Email(emailStr);
        Telephone telephone = new Telephone(telephoneStr);

        if (membreRepositoryPort.existeParEmail(email.value())) {
            throw new BusinessException("L'adresse email est déjà utilisée : " + email.value());
        }
        if (membreRepositoryPort.existeParTelephone(telephone.numero())) {
            throw new BusinessException("Le numéro de téléphone est déjà utilisé : " + telephone.numero());
        }

        MembreId id = MembreId.generate();

        Membre membre = new Membre(
                id,
                authUtilisateurId,
                nom,
                prenom,
                email,
                telephone,
                dateNaissance,
                adresse,
                ville,
                null,
                StatutMembre.ACTIF,
                LocalDateTime.now(),
                LocalDateTime.now(),
                new ArrayList<>()
        );

        Membre sauvegarde = membreRepositoryPort.sauvegarder(membre);

        eventPublisherPort.publishMembreInscrit(new MembreInscritEvent(
                sauvegarde.getId(),
                sauvegarde.getAuthUtilisateurId(),
                sauvegarde.getNom(),
                sauvegarde.getPrenom(),
                sauvegarde.getEmail().value(),
                sauvegarde.getTelephone().numero()
        ));

        return sauvegarde;
    }
}
