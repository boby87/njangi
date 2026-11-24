CREATE SCHEMA IF NOT EXISTS finances;
SET search_path TO finances;

-- =============================================================================
-- 1. COTISATIONS & POTS ROTATIFS
-- =============================================================================

CREATE TABLE IF NOT EXISTS type_cotisation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL,
    libelle VARCHAR(200) NOT NULL,
    description TEXT,
    categorie VARCHAR(50) NOT NULL DEFAULT 'ROTATIVE_POT',
    montant DECIMAL(15, 2) NOT NULL,
    est_rotatif BOOLEAN NOT NULL DEFAULT FALSE,
    est_obligatoire BOOLEAN NOT NULL DEFAULT TRUE,
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
    montant_du DECIMAL(15, 2) NOT NULL,
    montant_paye DECIMAL(15, 2) NOT NULL DEFAULT 0,
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
    montant_total DECIMAL(15, 2) NOT NULL,
    montant_net DECIMAL(15, 2),
    statut VARCHAR(50) NOT NULL DEFAULT 'PLANIFIE',
    mode_versement VARCHAR(50),
    reference_paiement VARCHAR(150),
    date_attribution TIMESTAMP,
    date_versement TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_type_cotisation_groupe ON type_cotisation (groupe_id);
CREATE INDEX IF NOT EXISTS idx_cotisation_reunion ON cotisation (reunion_id);
CREATE INDEX IF NOT EXISTS idx_cotisation_membre ON cotisation (membre_id);
CREATE INDEX IF NOT EXISTS idx_cotisation_groupe ON cotisation (groupe_id);
CREATE INDEX IF NOT EXISTS idx_cotisation_statut ON cotisation (statut);
CREATE INDEX IF NOT EXISTS idx_pot_session_groupe ON pot_session (groupe_id);
CREATE INDEX IF NOT EXISTS idx_pot_session_session ON pot_session (session_tontine_id);
CREATE INDEX IF NOT EXISTS idx_pot_session_membre ON pot_session (membre_beneficiaire_id);

-- =============================================================================
-- 2. PAIEMENTS (CASH AVEC RECU SIGNE & MOBILE MONEY IDEMPOTENT)
-- =============================================================================

CREATE TABLE IF NOT EXISTS paiement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cotisation_id UUID NOT NULL,
    membre_id UUID NOT NULL,
    groupe_id UUID NOT NULL,
    montant DECIMAL(15, 2) NOT NULL,
    mode_paiement VARCHAR(50) NOT NULL,
    cle_idempotence VARCHAR(150) NOT NULL UNIQUE,
    reference VARCHAR(100) UNIQUE,
    piece_jointe_url VARCHAR(500),
    numero_telephone VARCHAR(50),
    operateur VARCHAR(50),
    statut VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE_VALIDATION',
    valide_par UUID,
    date_validation TIMESTAMP,
    commentaire TEXT,
    date_paiement TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_paiement_cotisation ON paiement (cotisation_id);
CREATE INDEX IF NOT EXISTS idx_paiement_membre ON paiement (membre_id);
CREATE INDEX IF NOT EXISTS idx_paiement_groupe ON paiement (groupe_id);
CREATE INDEX IF NOT EXISTS idx_paiement_statut ON paiement (statut);
CREATE INDEX IF NOT EXISTS idx_paiement_reference ON paiement (reference);
CREATE INDEX IF NOT EXISTS idx_paiement_cle_idempotence ON paiement (cle_idempotence);

-- =============================================================================
-- 3. PENALITES & BAREME DISCIPLINAIRE
-- =============================================================================

CREATE TABLE IF NOT EXISTS tarification_penalite (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL,
    type_infraction VARCHAR(50) NOT NULL,
    montant NUMERIC(15, 2) NOT NULL CHECK (montant > 0),
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tarif_groupe_type UNIQUE (groupe_id, type_infraction)
);

CREATE TABLE IF NOT EXISTS penalite (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    membre_id UUID NOT NULL,
    groupe_id UUID NOT NULL,
    session_id UUID NOT NULL,
    reunion_id UUID,
    type_infraction VARCHAR(50) NOT NULL,
    montant NUMERIC(15, 2) NOT NULL CHECK (montant > 0),
    statut VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE',
    motif TEXT,
    date_paiement TIMESTAMP,
    cree_le TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_penalite_membre_id ON penalite (membre_id);
CREATE INDEX IF NOT EXISTS idx_penalite_groupe_id ON penalite (groupe_id);
CREATE INDEX IF NOT EXISTS idx_penalite_session_id ON penalite (session_id);
CREATE INDEX IF NOT EXISTS idx_penalite_reunion_id ON penalite (reunion_id);
CREATE INDEX IF NOT EXISTS idx_penalite_statut ON penalite (statut);
CREATE INDEX IF NOT EXISTS idx_tarif_groupe_id ON tarification_penalite (groupe_id);
