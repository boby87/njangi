CREATE SCHEMA IF NOT EXISTS cotisations;
SET search_path TO cotisations;

CREATE TABLE IF NOT EXISTS type_cotisation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL,
    libelle VARCHAR(200) NOT NULL,
    description TEXT,
    categorie VARCHAR(50) NOT NULL DEFAULT 'ROTATIVE_POT',
    montant DECIMAL(15,2) NOT NULL,
    est_rotatif BOOLEAN NOT NULL DEFAULT false,
    est_obligatoire BOOLEAN NOT NULL DEFAULT true,
    statut VARCHAR(50) NOT NULL DEFAULT 'ACTIF',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cotisation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL,
    membre_id UUID NOT NULL,
    type_cotisation_id UUID NOT NULL REFERENCES type_cotisation(id),
    reunion_id UUID NOT NULL,
    montant_du DECIMAL(15,2) NOT NULL,
    montant_paye DECIMAL(15,2) NOT NULL DEFAULT 0,
    statut VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    date_limite_paiement DATE,
    date_paiement TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pot_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL,
    session_tontine_id UUID NOT NULL,
    reunion_id UUID,
    membre_beneficiaire_id UUID NOT NULL,
    ordre_passage INT NOT NULL DEFAULT 1,
    montant_total DECIMAL(15,2) NOT NULL,
    montant_net DECIMAL(15,2),
    statut VARCHAR(50) NOT NULL DEFAULT 'PLANIFIE',
    mode_versement VARCHAR(50),
    reference_paiement VARCHAR(150),
    date_attribution TIMESTAMP,
    date_versement TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_type_cotisation_groupe ON type_cotisation(groupe_id);
CREATE INDEX IF NOT EXISTS idx_cotisation_reunion ON cotisation(reunion_id);
CREATE INDEX IF NOT EXISTS idx_cotisation_membre ON cotisation(membre_id);
CREATE INDEX IF NOT EXISTS idx_cotisation_groupe ON cotisation(groupe_id);
CREATE INDEX IF NOT EXISTS idx_cotisation_statut ON cotisation(statut);
CREATE INDEX IF NOT EXISTS idx_pot_session_groupe ON pot_session(groupe_id);
CREATE INDEX IF NOT EXISTS idx_pot_session_session ON pot_session(session_tontine_id);
CREATE INDEX IF NOT EXISTS idx_pot_session_membre ON pot_session(membre_beneficiaire_id);
