package com.njangi.reunions.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "participant_reunion", schema = "reunions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantReunion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reunion_id", nullable = false)
    private UUID reunionId;

    @Column(name = "membre_id", nullable = false)
    private UUID membreId;

    @Column(name = "present", nullable = false)
    @Builder.Default
    private Boolean present = false;

    @Column(name = "procuration", nullable = false)
    @Builder.Default
    private Boolean procuration = false;

    @Column(name = "heure_arrivee")
    private LocalDateTime heureArrivee;
}
