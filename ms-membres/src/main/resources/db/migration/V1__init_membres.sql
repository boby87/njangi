CREATE SCHEMA IF NOT EXISTS membres;

SET search_path TO membres;

CREATE TABLE IF NOT EXISTS membre (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_utilisateur_id UUID NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telephone VARCHAR(20) NOT NULL UNIQUE,
    date_naissance DATE,
    adresse TEXT,
    ville VARCHAR(100),
    photo_url VARCHAR(500),
    statut VARCHAR(50) NOT NULL DEFAULT 'ACTIF',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_membre_email ON membre(email);
CREATE INDEX idx_membre_telephone ON membre(telephone);
CREATE INDEX idx_membre_auth_id ON membre(auth_utilisateur_id);
