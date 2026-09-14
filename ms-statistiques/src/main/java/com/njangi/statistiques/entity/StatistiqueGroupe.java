package com.njangi.statistiques.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "statistique_groupe",
    schema = "statistiques",
    indexes = {
        @Index(name = "idx_stat_groupe_id",  columnList = "groupe_id"),
        @Index(name = "idx_stat_session_id", columnList = "session_id"),
        @Index(name = "idx_stat_calcule_le", columnList = "calcule_le")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_stat_groupe_session",
            columnNames = {"groupe_id", "session_id"}
        )
    }
)
public class StatistiqueGroupe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "groupe_id", nullable = false)
    private UUID groupeId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "total_collecte", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCollecte = BigDecimal.ZERO;

    @Column(name = "total_decaisse", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDecaisse = BigDecimal.ZERO;

    @Column(name = "solde_caisse", nullable = false, precision = 15, scale = 2)
    private BigDecimal soldeCaisse = BigDecimal.ZERO;

    @Column(name = "total_penalites", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPenalites = BigDecimal.ZERO;

    @Column(name = "total_cash", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCash = BigDecimal.ZERO;

    @Column(name = "total_momo", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalMomo = BigDecimal.ZERO;

    @Column(name = "nb_membres", nullable = false)
    private int nbMembres = 0;

    @Column(name = "nb_reunions", nullable = false)
    private int nbReunions = 0;

    @Column(name = "taux_participation", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxParticipation = BigDecimal.ZERO;

    @Column(name = "taux_presence", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxPresence = BigDecimal.ZERO;

    @Column(name = "calcule_le", nullable = false)
    private LocalDateTime calculeLe;

    public StatistiqueGroupe() {
    }

    public StatistiqueGroupe(UUID id, UUID groupeId, UUID sessionId, BigDecimal totalCollecte,
                             BigDecimal totalDecaisse, BigDecimal soldeCaisse, BigDecimal totalPenalites,
                             BigDecimal totalCash, BigDecimal totalMomo, int nbMembres, int nbReunions,
                             BigDecimal tauxParticipation, BigDecimal tauxPresence, LocalDateTime calculeLe) {
        this.id = id;
        this.groupeId = groupeId;
        this.sessionId = sessionId;
        this.totalCollecte = totalCollecte != null ? totalCollecte : BigDecimal.ZERO;
        this.totalDecaisse = totalDecaisse != null ? totalDecaisse : BigDecimal.ZERO;
        this.soldeCaisse = soldeCaisse != null ? soldeCaisse : BigDecimal.ZERO;
        this.totalPenalites = totalPenalites != null ? totalPenalites : BigDecimal.ZERO;
        this.totalCash = totalCash != null ? totalCash : BigDecimal.ZERO;
        this.totalMomo = totalMomo != null ? totalMomo : BigDecimal.ZERO;
        this.nbMembres = nbMembres;
        this.nbReunions = nbReunions;
        this.tauxParticipation = tauxParticipation != null ? tauxParticipation : BigDecimal.ZERO;
        this.tauxPresence = tauxPresence != null ? tauxPresence : BigDecimal.ZERO;
        this.calculeLe = calculeLe != null ? calculeLe : LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.calculeLe = LocalDateTime.now();
        if (this.totalCollecte == null) this.totalCollecte = BigDecimal.ZERO;
        if (this.totalDecaisse == null) this.totalDecaisse = BigDecimal.ZERO;
        if (this.totalPenalites == null) this.totalPenalites = BigDecimal.ZERO;
        if (this.totalCash == null) this.totalCash = BigDecimal.ZERO;
        if (this.totalMomo == null) this.totalMomo = BigDecimal.ZERO;
        this.soldeCaisse = this.totalCollecte.add(this.totalPenalites).subtract(this.totalDecaisse);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getGroupeId() {
        return groupeId;
    }

    public void setGroupeId(UUID groupeId) {
        this.groupeId = groupeId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public BigDecimal getTotalCollecte() {
        return totalCollecte;
    }

    public void setTotalCollecte(BigDecimal totalCollecte) {
        this.totalCollecte = totalCollecte;
    }

    public BigDecimal getTotalDecaisse() {
        return totalDecaisse;
    }

    public void setTotalDecaisse(BigDecimal totalDecaisse) {
        this.totalDecaisse = totalDecaisse;
    }

    public BigDecimal getSoldeCaisse() {
        return soldeCaisse;
    }

    public void setSoldeCaisse(BigDecimal soldeCaisse) {
        this.soldeCaisse = soldeCaisse;
    }

    public BigDecimal getTotalPenalites() {
        return totalPenalites;
    }

    public void setTotalPenalites(BigDecimal totalPenalites) {
        this.totalPenalites = totalPenalites;
    }

    public BigDecimal getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(BigDecimal totalCash) {
        this.totalCash = totalCash;
    }

    public BigDecimal getTotalMomo() {
        return totalMomo;
    }

    public void setTotalMomo(BigDecimal totalMomo) {
        this.totalMomo = totalMomo;
    }

    public int getNbMembres() {
        return nbMembres;
    }

    public void setNbMembres(int nbMembres) {
        this.nbMembres = nbMembres;
    }

    public int getNbReunions() {
        return nbReunions;
    }

    public void setNbReunions(int nbReunions) {
        this.nbReunions = nbReunions;
    }

    public BigDecimal getTauxParticipation() {
        return tauxParticipation;
    }

    public void setTauxParticipation(BigDecimal tauxParticipation) {
        this.tauxParticipation = tauxParticipation;
    }

    public BigDecimal getTauxPresence() {
        return tauxPresence;
    }

    public void setTauxPresence(BigDecimal tauxPresence) {
        this.tauxPresence = tauxPresence;
    }

    public LocalDateTime getCalculeLe() {
        return calculeLe;
    }

    public void setCalculeLe(LocalDateTime calculeLe) {
        this.calculeLe = calculeLe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatistiqueGroupe that = (StatistiqueGroupe) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "StatistiqueGroupe{" +
                "id=" + id +
                ", groupeId=" + groupeId +
                ", sessionId=" + sessionId +
                ", totalCollecte=" + totalCollecte +
                ", totalDecaisse=" + totalDecaisse +
                ", soldeCaisse=" + soldeCaisse +
                ", totalPenalites=" + totalPenalites +
                ", totalCash=" + totalCash +
                ", totalMomo=" + totalMomo +
                ", nbMembres=" + nbMembres +
                ", nbReunions=" + nbReunions +
                ", tauxParticipation=" + tauxParticipation +
                ", tauxPresence=" + tauxPresence +
                ", calculeLe=" + calculeLe +
                '}';
    }
}
