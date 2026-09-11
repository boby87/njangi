CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE auth.utilisateur (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    telephone     VARCHAR(20) UNIQUE NOT NULL,
    email         VARCHAR(255) UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    nom           VARCHAR(100),
    prenom        VARCHAR(100),
    photo_url     VARCHAR(500),
    ville         VARCHAR(100),
    pays          VARCHAR(100),
    statut        VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE_VERIFICATION',
    cree_le       TIMESTAMP NOT NULL DEFAULT NOW(),
    modifie_le    TIMESTAMP
);

CREATE TABLE auth.token_otp (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifiant VARCHAR(255) NOT NULL,
    code        VARCHAR(6) NOT NULL,
    expire_le   TIMESTAMP NOT NULL,
    utilise     BOOLEAN DEFAULT FALSE,
    cree_le     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_otp_identifiant ON auth.token_otp(identifiant);
