CREATE SCHEMA IF NOT EXISTS penalites;
SET search_path TO penalites;

CREATE TABLE IF NOT EXISTS tarification_penalite (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id       UUID            NOT NULL,
    type_infraction VARCHAR(50)     NOT NULL,
    montant         NUMERIC(15, 2)  NOT NULL CHECK (montant > 0),
    actif           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tarif_groupe_type UNIQUE (groupe_id, type_infraction)
);

CREATE TABLE IF NOT EXISTS penalite (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    membre_id       UUID            NOT NULL,
    groupe_id       UUID            NOT NULL,
    session_id      UUID            NOT NULL,
    reunion_id      UUID,
    type_infraction VARCHAR(50)     NOT NULL,
    montant         NUMERIC(15, 2)  NOT NULL CHECK (montant > 0),
    statut          VARCHAR(30)     NOT NULL DEFAULT 'EN_ATTENTE',
    motif           TEXT,
    date_paiement   TIMESTAMP,
    cree_le         TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_penalite_membre_id  ON penalite (membre_id);
CREATE INDEX IF NOT EXISTS idx_penalite_groupe_id  ON penalite (groupe_id);
CREATE INDEX IF NOT EXISTS idx_penalite_session_id ON penalite (session_id);
CREATE INDEX IF NOT EXISTS idx_penalite_reunion_id ON penalite (reunion_id);
CREATE INDEX IF NOT EXISTS idx_penalite_statut     ON penalite (statut);
CREATE INDEX IF NOT EXISTS idx_tarif_groupe_id     ON tarification_penalite (groupe_id);
