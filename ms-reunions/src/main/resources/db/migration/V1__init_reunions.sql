CREATE SCHEMA IF NOT EXISTS reunions;

CREATE TABLE IF NOT EXISTS reunions.reunion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL,
    session_tontine_id UUID,
    titre VARCHAR(300) NOT NULL,
    date_reunion TIMESTAMP NOT NULL,
    lieu_reunion TEXT,
    type_siege VARCHAR(20) NOT NULL DEFAULT 'FIXE',
    hote_id UUID,
    statut VARCHAR(50) NOT NULL DEFAULT 'PLANIFIEE',
    ordre_jour TEXT,
    compte_rendu TEXT,
    president_reunion_id UUID,
    secretaire_reunion_id UUID,
    tresorier_reunion_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS reunions.presence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reunion_id UUID NOT NULL REFERENCES reunions.reunion(id) ON DELETE CASCADE,
    membre_id UUID NOT NULL,
    statut VARCHAR(30) NOT NULL DEFAULT 'ABSENT',
    heure_arrivee TIMESTAMP,
    justification TEXT,
    procuration BOOLEAN NOT NULL DEFAULT false,
    mandataire_id UUID,
    enregistre_le TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_reunion_membre UNIQUE (reunion_id, membre_id)
);

CREATE INDEX IF NOT EXISTS idx_reunion_groupe ON reunions.reunion(groupe_id);
CREATE INDEX IF NOT EXISTS idx_reunion_session ON reunions.reunion(session_tontine_id);
CREATE INDEX IF NOT EXISTS idx_reunion_statut ON reunions.reunion(statut);
CREATE INDEX IF NOT EXISTS idx_reunion_date ON reunions.reunion(date_reunion);
CREATE INDEX IF NOT EXISTS idx_presence_reunion ON reunions.presence(reunion_id);
CREATE INDEX IF NOT EXISTS idx_presence_membre ON reunions.presence(membre_id);
