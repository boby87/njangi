CREATE SCHEMA IF NOT EXISTS paiements;
SET search_path TO paiements;

CREATE TABLE IF NOT EXISTS paiement (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cotisation_id UUID NOT NULL,
    membre_id UUID NOT NULL,
    groupe_id UUID NOT NULL,
    montant DECIMAL(15,2) NOT NULL,
    mode_paiement VARCHAR(50) NOT NULL,
    reference VARCHAR(100) UNIQUE,
    statut VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    date_paiement TIMESTAMP NOT NULL DEFAULT NOW(),
    valide_par UUID,
    date_validation TIMESTAMP,
    commentaire TEXT,
    otp_verifie BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_paiement_cotisation ON paiement(cotisation_id);
CREATE INDEX idx_paiement_membre ON paiement(membre_id);
CREATE INDEX idx_paiement_groupe ON paiement(groupe_id);
CREATE INDEX idx_paiement_statut ON paiement(statut);
CREATE INDEX idx_paiement_reference ON paiement(reference);
