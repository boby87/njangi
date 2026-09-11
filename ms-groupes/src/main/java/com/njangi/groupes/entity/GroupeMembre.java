package com.njangi.groupes.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "groupe_membre", schema = "groupes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupeMembre {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "membre_id", nullable = false)
    private UUID membreId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private RoleMembre role;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutMembreGroupe statut;

    @Column(name = "date_adhesion", nullable = false)
    private LocalDateTime dateAdhesion;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @PrePersist
    protected void onCreate() {
        dateAdhesion = LocalDateTime.now();
        if (statut == null) statut = StatutMembreGroupe.ACTIF;
    }
}
