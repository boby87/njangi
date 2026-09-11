package com.njangi.membres.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "membre_groupe", schema = "membres")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MembreGroupe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID utilisateurId;

    @Column(nullable = false)
    private UUID groupeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleMembre role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutMembre statut;

    private LocalDateTime rejointLe;
    private LocalDateTime modifieLe;

    public enum RoleMembre {
        CREATEUR, PRESIDENT, TRESORIER, SECRETAIRE, AUDITEUR, MEMBRE
    }

    public enum StatutMembre {
        EN_ATTENTE, ACTIF, SUSPENDU, EXCLU
    }
}
