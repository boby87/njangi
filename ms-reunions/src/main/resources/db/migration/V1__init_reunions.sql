CREATE SCHEMA IF NOT EXISTS reunions;
SET search_path TO reunions;

CREATE TABLE IF NOT EXISTS reunion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL,
    session_tontine_id UUID,
    titre VARCHAR(300) NOT NULL,
    date_reunion TIMESTAMP NOT NULL,
    lieu_reunion TEXT,
    type_siege VARCHAR(20) NOT NULL DEFAULT 'FIXE',
    statut VARCHAR(50) NOT NULL DEFAULT 'PLANIFIEE',
    ordre_jour TEXT,
    compte_rendu TEXT,
    president_reunion_id UUID,
    tresorier_reunion_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS participant_reunion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reunion_id UUID NOT NULL REFERENCES reunion(id),
    membre_id UUID NOT NULL,
    present BOOLEAN NOT NULL DEFAULT false,
    procuration BOOLEAN NOT NULL DEFAULT false,
    heure_arrivee TIMESTAMP,
    UNIQUE(reunion_id, membre_id)
);

CREATE INDEX idx_reunion_groupe ON reunion(groupe_id);
CREATE INDEX idx_reunion_statut ON reunion(statut);
CREATE INDEX idx_participant_reunion ON participant_reunion(reunion_id);
