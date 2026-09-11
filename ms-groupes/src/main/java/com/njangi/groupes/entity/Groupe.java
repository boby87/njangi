package com.njangi.groupes.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "groupe", schema = "groupes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Groupe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "description")
    private String description;

    @Column(name = "createur_membre_id", nullable = false)
    private UUID createurMembreId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_siege", nullable = false)
    private TypeSiege typeSiege;

    @Column(name = "adresse_siege")
    private String adresseSiege;

    @Column(name = "montant_cotisation_principale", nullable = false)
    private BigDecimal montantCotisationPrincipale;

    @Column(name = "frequence_reunion", nullable = false)
    private String frequenceReunion;

    @Column(name = "nombre_membres_max")
    private Integer nombreMembresMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutGroupe statut;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (statut == null) statut = StatutGroupe.ACTIF;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
