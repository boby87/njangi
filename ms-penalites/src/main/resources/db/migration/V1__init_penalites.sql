-- Migration V1 : Initialisation du schéma penalites
-- Microservice ms-penalites — Application Njangi Tontine

CREATE SCHEMA IF NOT EXISTS penalites;

-- Table de tarification des pénalités par groupe
CREATE TABLE penalites.tarification_penalite (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id       UUID            NOT NULL,
    type_infraction VARCHAR(30)     NOT NULL
                                    CHECK (type_infraction IN (
                                        'RETARD_PAIEMENT', 'ABSENCE',
                                        'RETARD_REUNION', 'NON_RESPECT_REGLES'
                                    )),
    montant         NUMERIC(15, 2)  NOT NULL CHECK (montant > 0),
    actif           BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_tarif_groupe_type UNIQUE (groupe_id, type_infraction)
);

-- Table des pénalités appliquées aux membres
CREATE TABLE penalites.penalite (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    membre_id       UUID            NOT NULL,
    groupe_id       UUID            NOT NULL,
    session_id      UUID            NOT NULL,
    type_infraction VARCHAR(30)     NOT NULL
                                    CHECK (type_infraction IN (
                                        'RETARD_PAIEMENT', 'ABSENCE',
                                        'RETARD_REUNION', 'NON_RESPECT_REGLES'
                                    )),
    montant         NUMERIC(15, 2)  NOT NULL CHECK (montant > 0),
    statut          VARCHAR(20)     NOT NULL DEFAULT 'EN_ATTENTE'
                                    CHECK (statut IN ('EN_ATTENTE', 'PAYEE', 'ANNULEE')),
    cree_le         TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Index de recherche fréquente
CREATE INDEX idx_penalite_membre_id  ON penalites.penalite (membre_id);
CREATE INDEX idx_penalite_groupe_id  ON penalites.penalite (groupe_id);
CREATE INDEX idx_penalite_session_id ON penalites.penalite (session_id);
CREATE INDEX idx_penalite_statut     ON penalites.penalite (statut);
CREATE INDEX idx_tarif_groupe_id     ON penalites.tarification_penalite (groupe_id);

COMMENT ON TABLE  penalites.penalite              IS 'Pénalités appliquées aux membres Njangi';
COMMENT ON TABLE  penalites.tarification_penalite IS 'Barème des pénalités par groupe et type d''infraction';
COMMENT ON COLUMN penalites.penalite.statut       IS 'EN_ATTENTE | PAYEE | ANNULEE';
