-- Migration V1 : Initialisation du schéma engagement
-- Microservice ms-engagement (Notifications & Statistiques) — Application Njangi Tontine

CREATE SCHEMA IF NOT EXISTS engagement;

-- =============================================================================
-- 1. Table notification
-- =============================================================================
CREATE TABLE engagement.notification (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    destinataire_id      UUID         NOT NULL,
    groupe_id            UUID,
    canal                VARCHAR(10)  NOT NULL CHECK (canal IN ('PUSH', 'SMS', 'EMAIL', 'IN_APP')),
    type                 VARCHAR(100) NOT NULL,
    titre                VARCHAR(255) NOT NULL,
    contenu              TEXT         NOT NULL,
    statut               VARCHAR(15)  NOT NULL DEFAULT 'EN_ATTENTE'
                                      CHECK (statut IN ('EN_ATTENTE', 'ENVOYEE', 'ECHEC')),
    lue                  BOOLEAN      NOT NULL DEFAULT FALSE,
    cree_le              TIMESTAMP    NOT NULL DEFAULT NOW(),
    envoyee_le           TIMESTAMP,
    reference_objet      VARCHAR(100),
    type_objet           VARCHAR(50),
    destinataire_contact VARCHAR(255)
);

CREATE INDEX idx_notif_dest_id   ON engagement.notification (destinataire_id);
CREATE INDEX idx_notif_statut    ON engagement.notification (statut);
CREATE INDEX idx_notif_groupe_id ON engagement.notification (groupe_id);
CREATE INDEX idx_notif_lue       ON engagement.notification (lue);
CREATE INDEX idx_notif_dest_lue  ON engagement.notification (destinataire_id, lue);

COMMENT ON TABLE  engagement.notification                 IS 'Notifications envoyées aux membres Njangi (Push, SMS, Email, In-App)';
COMMENT ON COLUMN engagement.notification.canal           IS 'PUSH | SMS | EMAIL | IN_APP';
COMMENT ON COLUMN engagement.notification.statut          IS 'EN_ATTENTE | ENVOYEE | ECHEC';
COMMENT ON COLUMN engagement.notification.destinataire_id IS 'UUID du membre destinataire';

-- =============================================================================
-- 2. Table statistique_groupe
-- =============================================================================
CREATE TABLE engagement.statistique_groupe (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id           UUID            NOT NULL,
    session_id          UUID            NOT NULL,
    total_collecte      NUMERIC(15, 2)  NOT NULL DEFAULT 0 CHECK (total_collecte >= 0),
    total_decaisse      NUMERIC(15, 2)  NOT NULL DEFAULT 0 CHECK (total_decaisse >= 0),
    solde_caisse        NUMERIC(15, 2)  NOT NULL DEFAULT 0,
    total_penalites     NUMERIC(15, 2)  NOT NULL DEFAULT 0 CHECK (total_penalites >= 0),
    total_cash          NUMERIC(15, 2)  NOT NULL DEFAULT 0 CHECK (total_cash >= 0),
    total_momo          NUMERIC(15, 2)  NOT NULL DEFAULT 0 CHECK (total_momo >= 0),
    nb_membres          INTEGER         NOT NULL DEFAULT 0 CHECK (nb_membres >= 0),
    nb_reunions         INTEGER         NOT NULL DEFAULT 0 CHECK (nb_reunions >= 0),
    taux_participation  NUMERIC(5, 2)   NOT NULL DEFAULT 0
                                        CHECK (taux_participation >= 0 AND taux_participation <= 100),
    taux_presence       NUMERIC(5, 2)   NOT NULL DEFAULT 0
                                        CHECK (taux_presence >= 0 AND taux_presence <= 100),
    calcule_le          TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_stat_groupe_session UNIQUE (groupe_id, session_id)
);

CREATE INDEX idx_stat_groupe_id  ON engagement.statistique_groupe (groupe_id);
CREATE INDEX idx_stat_session_id ON engagement.statistique_groupe (session_id);
CREATE INDEX idx_stat_calcule_le ON engagement.statistique_groupe (calcule_le DESC);

COMMENT ON TABLE  engagement.statistique_groupe                    IS 'Agrégats statistiques par groupe et session Njangi';
COMMENT ON COLUMN engagement.statistique_groupe.total_collecte     IS 'Montant total des cotisations validées sur la session (FCFA)';
COMMENT ON COLUMN engagement.statistique_groupe.total_decaisse     IS 'Montant total des pots et secours versés (FCFA)';
COMMENT ON COLUMN engagement.statistique_groupe.solde_caisse       IS 'Solde net en caisse = total_collecte + total_penalites - total_decaisse';
COMMENT ON COLUMN engagement.statistique_groupe.total_penalites    IS 'Amendes et pénalités perçues (FCFA)';
COMMENT ON COLUMN engagement.statistique_groupe.total_cash         IS 'Total encaissé en espèces/cash (FCFA)';
COMMENT ON COLUMN engagement.statistique_groupe.total_momo         IS 'Total encaissé par Mobile Money MTN/Orange (FCFA)';
COMMENT ON COLUMN engagement.statistique_groupe.taux_participation IS 'Taux de recouvrement des cotisations (0–100)';
COMMENT ON COLUMN engagement.statistique_groupe.taux_presence      IS 'Ratio de présence physique aux réunions (0–100)';
COMMENT ON COLUMN engagement.statistique_groupe.calcule_le         IS 'Timestamp du dernier recalcul';
