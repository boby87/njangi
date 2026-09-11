package com.njangi.groupes.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "mandat_bureau", schema = "groupes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MandatBureau {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false) private UUID groupeId;
    @Column(nullable = false) private UUID presidentId;
    private UUID tresorierMembreId;
    private UUID secretaireMembreId;
    @Column(nullable = false) private LocalDate dateDebut;
    private LocalDate dateFin;
    @Enumerated(EnumType.STRING) private StatutMandat statut;
    private LocalDateTime creeLe;

    public enum StatutMandat { ACTIF, TERMINE }
}
