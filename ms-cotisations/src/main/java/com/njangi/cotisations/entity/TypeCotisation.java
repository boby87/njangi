package com.njangi.cotisations.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "type_cotisation", schema = "cotisations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeCotisation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "libelle", nullable = false, length = 200)
    private String libelle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "montant", nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(name = "est_rotatif", nullable = false)
    @Builder.Default
    private Boolean estRotatif = false;

    @Column(name = "est_obligatoire", nullable = false)
    @Builder.Default
    private Boolean estObligatoire = true;

    @Column(name = "statut", length = 50)
    @Builder.Default
    private String statut = "ACTIF";
}
