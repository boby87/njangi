package com.njangi.identite.application.service;

import com.njangi.identite.domain.model.Membre;
import com.njangi.identite.domain.model.MembreId;
import com.njangi.identite.domain.port.in.ConsulterMembreUseCase;
import com.njangi.identite.domain.port.out.MembreRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConsulterMembreService implements ConsulterMembreUseCase {

    private final MembreRepositoryPort membreRepositoryPort;

    public ConsulterMembreService(MembreRepositoryPort membreRepositoryPort) {
        this.membreRepositoryPort = membreRepositoryPort;
    }

    @Override
    public List<Membre> listerTous() {
        return membreRepositoryPort.trouverTous();
    }

    @Override
    public Optional<Membre> trouverParId(MembreId id) {
        return membreRepositoryPort.trouverParId(id);
    }

    @Override
    public Optional<Membre> trouverParAuthId(UUID authUtilisateurId) {
        return membreRepositoryPort.trouverParAuthId(authUtilisateurId);
    }

    @Override
    public Optional<Membre> trouverParTelephone(String telephone) {
        return membreRepositoryPort.trouverParTelephone(telephone);
    }

    @Override
    public Optional<Membre> trouverParEmail(String email) {
        return membreRepositoryPort.trouverParEmail(email);
    }
}
