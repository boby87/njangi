-- Migration consolidée V1 pour ms-identite (fusion de ms-auth V1/V2 et ms-membres V1/V2)
CREATE SCHEMA IF NOT EXISTS identite;

CREATE TABLE IF NOT EXISTS identite.utilisateurs (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    telephone          VARCHAR(30) UNIQUE,
    email              VARCHAR(255) UNIQUE,
    mot_de_passe_hash  VARCHAR(255),
    nom                VARCHAR(100),
    prenom             VARCHAR(100),
    photo_url          VARCHAR(500),
    ville              VARCHAR(100),
    pays               VARCHAR(100),
    statut             VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE_VERIFICATION',
    provider_auth      VARCHAR(30) NOT NULL DEFAULT 'LOCAL',
    provider_id        VARCHAR(255),
    cree_le            TIMESTAMP NOT NULL DEFAULT NOW(),
    modifie_le         TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_utilisateurs_provider
    ON identite.utilisateurs(provider_auth, provider_id);

CREATE TABLE IF NOT EXISTS identite.token_otp (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifiant VARCHAR(255) NOT NULL,
    code        VARCHAR(6) NOT NULL,
    expire_le   TIMESTAMP NOT NULL,
    utilise     BOOLEAN NOT NULL DEFAULT FALSE,
    cree_le     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_otp_identifiant
    ON identite.token_otp(identifiant);

CREATE TABLE IF NOT EXISTS identite.membres (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_utilisateur_id UUID NOT NULL UNIQUE,
    nom                 VARCHAR(100) NOT NULL,
    prenom              VARCHAR(100) NOT NULL,
    email               VARCHAR(255) NOT NULL UNIQUE,
    telephone           VARCHAR(30) NOT NULL UNIQUE,
    date_naissance      DATE,
    adresse             TEXT,
    ville               VARCHAR(100),
    photo_url           VARCHAR(500),
    statut              VARCHAR(50) NOT NULL DEFAULT 'ACTIF',
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_membres_email
    ON identite.membres(email);
CREATE INDEX IF NOT EXISTS idx_membres_telephone
    ON identite.membres(telephone);
CREATE INDEX IF NOT EXISTS idx_membres_auth_id
    ON identite.membres(auth_utilisateur_id);

CREATE TABLE IF NOT EXISTS identite.membre_groupes (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    utilisateur_id UUID NOT NULL,
    groupe_id      UUID NOT NULL,
    role           VARCHAR(50) NOT NULL DEFAULT 'MEMBRE',
    statut         VARCHAR(50) NOT NULL DEFAULT 'ACTIF',
    rejoint_le     TIMESTAMP NOT NULL DEFAULT NOW(),
    modifie_le     TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_membre_groupes UNIQUE (utilisateur_id, groupe_id)
);

CREATE INDEX IF NOT EXISTS idx_membre_groupes_user
    ON identite.membre_groupes(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_membre_groupes_grp
    ON identite.membre_groupes(groupe_id);
