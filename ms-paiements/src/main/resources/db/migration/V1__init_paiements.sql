CREATE SCHEMA IF NOT EXISTS paiements;
SET search_path TO paiements;

CREATE TABLE IF NOT EXISTS paiement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cotisation_id UUID NOT NULL,
    membre_id UUID NOT NULL,
    groupe_id UUID NOT NULL,
    montant DECIMAL(15,2) NOT NULL,
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

CREATE INDEX IF NOT EXISTS idx_paiement_cotisation ON paiement(cotisation_id);
CREATE INDEX IF NOT EXISTS idx_paiement_membre ON paiement(membre_id);
CREATE INDEX IF NOT EXISTS idx_paiement_groupe ON paiement(groupe_id);
CREATE INDEX IF NOT EXISTS idx_paiement_statut ON paiement(statut);
CREATE INDEX IF NOT EXISTS idx_paiement_reference ON paiement(reference);
CREATE INDEX IF NOT EXISTS idx_paiement_cle_idempotence ON paiement(cle_idempotence);
