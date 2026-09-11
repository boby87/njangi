package com.njangi.penalites.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
    name = "tarification_penalite",
    schema = "penalites",
    indexes = {
        @Index(name = "idx_tarif_groupe_id", columnList = "groupe_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_tarif_groupe_type",
            columnNames = {"groupe_id", "type_infraction"}
        )
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarificationPenalite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_infraction", nullable = false, length = 30)
    private TypeInfraction typeInfraction;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;
}
