package com.njangi.reunions.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "presence", schema = "reunions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Presence {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false) private UUID reunionId;
    @Column(nullable = false) private UUID membreId;
    @Enumerated(EnumType.STRING) private StatutPresence statut;
    private String justification;
    private LocalDateTime enregistreLe;

    public enum StatutPresence { PRESENT, ABSENT, RETARD, EXCUSE }
}
