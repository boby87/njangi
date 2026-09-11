CREATE SCHEMA IF NOT EXISTS groupes;

SET search_path TO groupes;

CREATE TABLE IF NOT EXISTS groupe (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom VARCHAR(200) NOT NULL,
    description TEXT,
    createur_membre_id UUID NOT NULL,
    type_siege VARCHAR(20) NOT NULL CHECK (type_siege IN ('FIXE', 'ROTATIF')),
    adresse_siege TEXT,
    montant_cotisation_principale DECIMAL(15,2) NOT NULL,
    frequence_reunion VARCHAR(50) NOT NULL,
    nombre_membres_max INTEGER,
    statut VARCHAR(50) NOT NULL DEFAULT 'ACTIF',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS groupe_membre (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL REFERENCES groupe(id),
    membre_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('CREATEUR','PRESIDENT','TRESORIER','SECRETAIRE','AUDITEUR','MEMBRE')),
    statut VARCHAR(50) NOT NULL DEFAULT 'ACTIF',
    date_adhesion TIMESTAMP NOT NULL DEFAULT NOW(),
    date_fin TIMESTAMP,
    UNIQUE(groupe_id, membre_id)
);

CREATE TABLE IF NOT EXISTS session_tontine (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL REFERENCES groupe(id),
    libelle VARCHAR(200) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    statut VARCHAR(50) NOT NULL DEFAULT 'EN_COURS',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS mandat_bureau (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL REFERENCES groupe(id),
    president_membre_id UUID NOT NULL,
    tresorier_membre_id UUID,
    secretaire_membre_id UUID,
    date_debut DATE NOT NULL,
    date_fin DATE,
    statut VARCHAR(50) NOT NULL DEFAULT 'EN_COURS',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_groupe_createur ON groupe(createur_membre_id);
CREATE INDEX idx_groupe_membre_groupe ON groupe_membre(groupe_id);
CREATE INDEX idx_groupe_membre_membre ON groupe_membre(membre_id);
