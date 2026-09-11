-- Migration V1 : Initialisation du schéma statistiques
-- Microservice ms-statistiques — Application Njangi Tontine

CREATE SCHEMA IF NOT EXISTS statistiques;

CREATE TABLE statistiques.statistique_groupe (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id           UUID            NOT NULL,
    session_id          UUID            NOT NULL,
    total_collecte      NUMERIC(15, 2)  NOT NULL DEFAULT 0 CHECK (total_collecte >= 0),
    nb_membres          INTEGER         NOT NULL DEFAULT 0 CHECK (nb_membres >= 0),
    -- taux_participation : pourcentage 0.00 – 100.00
    taux_participation  NUMERIC(5, 2)   NOT NULL DEFAULT 0
                                        CHECK (taux_participation >= 0 AND taux_participation <= 100),
    -- taux_presence : pourcentage 0.00 – 100.00
    taux_presence       NUMERIC(5, 2)   NOT NULL DEFAULT 0
                                        CHECK (taux_presence >= 0 AND taux_presence <= 100),
    calcule_le          TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_stat_groupe_session UNIQUE (groupe_id, session_id)
);

-- Index de recherche fréquente
CREATE INDEX idx_stat_groupe_id  ON statistiques.statistique_groupe (groupe_id);
CREATE INDEX idx_stat_session_id ON statistiques.statistique_groupe (session_id);
CREATE INDEX idx_stat_calcule_le ON statistiques.statistique_groupe (calcule_le DESC);

COMMENT ON TABLE  statistiques.statistique_groupe                 IS 'Agrégats statistiques par groupe et session Njangi';
COMMENT ON COLUMN statistiques.statistique_groupe.total_collecte  IS 'Montant total des paiements validés sur la session (FCFA)';
COMMENT ON COLUMN statistiques.statistique_groupe.taux_participation IS 'Ratio paiements effectués vs attendus (0–100)';
COMMENT ON COLUMN statistiques.statistique_groupe.taux_presence      IS 'Ratio présences aux réunions (0–100)';
COMMENT ON COLUMN statistiques.statistique_groupe.calcule_le         IS 'Timestamp du dernier recalcul (mis à jour à chaque écriture)';
