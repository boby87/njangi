package com.njangi.notifications.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "notification",
    schema = "notifications",
    indexes = {
        @Index(name = "idx_notif_dest_id",   columnList = "destinataire_id"),
        @Index(name = "idx_notif_statut",    columnList = "statut"),
        @Index(name = "idx_notif_groupe_id", columnList = "groupe_id"),
        @Index(name = "idx_notif_lue",       columnList = "lue")
    }
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "destinataire_id", nullable = false)
    private UUID destinataireId;

    @Column(name = "groupe_id")
    private UUID groupeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Canal canal;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatutNotification statut = StatutNotification.EN_ATTENTE;

    @Column(nullable = false)
    private boolean lue = false;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @Column(name = "envoyee_le")
    private LocalDateTime envoyeeLe;

    @Column(name = "reference_objet", length = 100)
    private String referenceObjet;

    @Column(name = "type_objet", length = 50)
    private String typeObjet;

    @Column(name = "destinataire_contact", length = 255)
    private String destinataireContact;

    public Notification() {
    }

    public Notification(UUID id, UUID destinataireId, UUID groupeId, Canal canal, String type,
                        String titre, String contenu, StatutNotification statut, boolean lue,
                        LocalDateTime creeLe, LocalDateTime envoyeeLe, String referenceObjet,
                        String typeObjet, String destinataireContact) {
        this.id = id;
        this.destinataireId = destinataireId;
        this.groupeId = groupeId;
        this.canal = canal;
        this.type = type;
        this.titre = titre;
        this.contenu = contenu;
        this.statut = statut != null ? statut : StatutNotification.EN_ATTENTE;
        this.lue = lue;
        this.creeLe = creeLe;
        this.envoyeeLe = envoyeeLe;
        this.referenceObjet = referenceObjet;
        this.typeObjet = typeObjet;
        this.destinataireContact = destinataireContact;
    }

    @PrePersist
    protected void onCreate() {
        if (this.creeLe == null) {
            this.creeLe = LocalDateTime.now();
        }
        if (this.statut == null) {
            this.statut = StatutNotification.EN_ATTENTE;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDestinataireId() {
        return destinataireId;
    }

    public void setDestinataireId(UUID destinataireId) {
        this.destinataireId = destinataireId;
    }

    public UUID getGroupeId() {
        return groupeId;
    }

    public void setGroupeId(UUID groupeId) {
        this.groupeId = groupeId;
    }

    public Canal getCanal() {
        return canal;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public StatutNotification getStatut() {
        return statut;
    }

    public void setStatut(StatutNotification statut) {
        this.statut = statut;
    }

    public boolean isLue() {
        return lue;
    }

    public void setLue(boolean lue) {
        this.lue = lue;
    }

    public LocalDateTime getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(LocalDateTime creeLe) {
        this.creeLe = creeLe;
    }

    public LocalDateTime getEnvoyeeLe() {
        return envoyeeLe;
    }

    public void setEnvoyeeLe(LocalDateTime envoyeeLe) {
        this.envoyeeLe = envoyeeLe;
    }

    public String getReferenceObjet() {
        return referenceObjet;
    }

    public void setReferenceObjet(String referenceObjet) {
        this.referenceObjet = referenceObjet;
    }

    public String getTypeObjet() {
        return typeObjet;
    }

    public void setTypeObjet(String typeObjet) {
        this.typeObjet = typeObjet;
    }

    public String getDestinataireContact() {
        return destinataireContact;
    }

    public void setDestinataireContact(String destinataireContact) {
        this.destinataireContact = destinataireContact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", destinataireId=" + destinataireId +
                ", canal=" + canal +
                ", type='" + type + '\'' +
                ", titre='" + titre + '\'' +
                ", statut=" + statut +
                ", lue=" + lue +
                ", creeLe=" + creeLe +
                '}';
    }
}
