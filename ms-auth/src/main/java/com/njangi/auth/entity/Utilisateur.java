package com.njangi.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "utilisateur", schema = "auth")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String telephone;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String motDePasseHash;

    private String nom;
    private String prenom;
    private String photoUrl;
    private String ville;
    private String pays;

    @Enumerated(EnumType.STRING)
    private StatutCompte statut;

    private LocalDateTime creeLe;
    private LocalDateTime modifieLe;

    public enum StatutCompte { ACTIF, SUSPENDU, EN_ATTENTE_VERIFICATION }
}
