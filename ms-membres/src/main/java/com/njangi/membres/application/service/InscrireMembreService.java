package com.njangi.membres.application.service;

import com.njangi.membres.domain.event.MembreInscritEvent;
import com.njangi.membres.domain.model.Email;
import com.njangi.membres.domain.model.Membre;
import com.njangi.membres.domain.model.MembreId;
import com.njangi.membres.domain.model.StatutMembre;
import com.njangi.membres.domain.model.Telephone;
import com.njangi.membres.domain.port.in.InscrireMembreUseCase;
import com.njangi.membres.domain.port.out.MembreEventPublisherPort;
import com.njangi.membres.domain.port.out.MembreRepositoryPort;
import com.njangi.membres.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class InscrireMembreService implements InscrireMembreUseCase {

    private final MembreRepositoryPort membreRepositoryPort;
    private final MembreEventPublisherPort eventPublisherPort;

    public InscrireMembreService(MembreRepositoryPort membreRepositoryPort, MembreEventPublisherPort eventPublisherPort) {
        this.membreRepositoryPort = membreRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    @Transactional
    public Membre inscrire(UUID authUtilisateurId, String nom, String prenom,
                           String emailStr, String telephoneStr, LocalDate dateNaissance,
                           String adresse, String ville) {
        if (membreRepositoryPort.existeParEmail(emailStr)) {
            throw new BusinessException("L'adresse email est déjà utilisée : " + emailStr);
        }
        if (membreRepositoryPort.existeParTelephone(telephoneStr)) {
            throw new BusinessException("Le numéro de téléphone est déjà utilisé : " + telephoneStr);
        }

        Email email = new Email(emailStr);
        Telephone telephone = new Telephone(telephoneStr);
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

        eventPublisherPort.publierMembreInscrit(new MembreInscritEvent(
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
