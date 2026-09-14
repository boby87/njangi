SET search_path TO membres;

CREATE TABLE IF NOT EXISTS membre_groupe (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    utilisateur_id UUID NOT NULL,
    groupe_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBRE',
    statut VARCHAR(50) NOT NULL DEFAULT 'ACTIF',
    rejoint_le TIMESTAMP NOT NULL DEFAULT NOW(),
    modifie_le TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_membre_groupe UNIQUE (utilisateur_id, groupe_id)
);

CREATE INDEX IF NOT EXISTS idx_membre_groupe_user ON membre_groupe(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_membre_groupe_grp ON membre_groupe(groupe_id);
