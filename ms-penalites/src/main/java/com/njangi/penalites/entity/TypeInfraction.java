package com.njangi.penalites.entity;

/**
 * Types d'infraction pouvant donner lieu à une pénalité dans un groupe Njangi.
 */
public enum TypeInfraction {
    /** Paiement de cotisation effectué après la date limite */
    RETARD_PAIEMENT,
    /** Absence non justifiée à une réunion */
    ABSENCE,
    /** Arrivée tardive à une réunion */
    RETARD_REUNION,
    /** Non-respect du règlement intérieur du groupe */
    NON_RESPECT_REGLES
}
