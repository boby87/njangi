package com.njangi.reunions.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reunion", schema = "reunions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reunion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "session_tontine_id")
    private UUID sessionTontineId;

    @Column(name = "titre", nullable = false, length = 300)
    private String titre;

    @Column(name = "date_reunion", nullable = false)
    private LocalDateTime dateReunion;

    @Column(name = "lieu_reunion", columnDefinition = "TEXT")
    private String lieuReunion;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_siege", nullable = false, length = 20)
    private TypeSiege typeSiege;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 50)
    @Builder.Default
    private StatutReunion statut = StatutReunion.PLANIFIEE;

    @Column(name = "ordre_jour", columnDefinition = "TEXT")
    private String ordreJour;

    @Column(name = "compte_rendu", columnDefinition = "TEXT")
    private String compteRendu;

    @Column(name = "president_reunion_id")
    private UUID presidentReunionId;

    @Column(name = "tresorier_reunion_id")
    private UUID tresorierReunionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
