package com.njangi.groupes.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "session", schema = "groupes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Session {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false) private UUID groupeId;
    @Column(nullable = false) private String libelle;
    @Column(nullable = false) private LocalDate dateDebut;
    @Column(nullable = false) private LocalDate dateFin;
    @Enumerated(EnumType.STRING) private StatutSession statut;
    private LocalDateTime creeLe;

    public enum StatutSession { EN_COURS, TERMINEE, ANNULEE }
}
