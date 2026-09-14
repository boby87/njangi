-- Migration V2: Support de l'authentification sociale (Google, Facebook, OAuth2)
ALTER TABLE auth.utilisateur ALTER COLUMN telephone DROP NOT NULL;
ALTER TABLE auth.utilisateur ALTER COLUMN telephone TYPE VARCHAR(30);
ALTER TABLE auth.utilisateur ALTER COLUMN mot_de_passe_hash DROP NOT NULL;

ALTER TABLE auth.utilisateur ADD COLUMN IF NOT EXISTS provider_auth VARCHAR(30) NOT NULL DEFAULT 'LOCAL';
ALTER TABLE auth.utilisateur ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_utilisateur_provider ON auth.utilisateur(provider_auth, provider_id);
