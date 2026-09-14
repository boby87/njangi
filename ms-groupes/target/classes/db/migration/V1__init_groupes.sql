CREATE SCHEMA IF NOT EXISTS groupes;

CREATE TABLE IF NOT EXISTS groupes.groupe (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom VARCHAR(200) NOT NULL,
    description TEXT,
    createur_membre_id UUID NOT NULL,
    type_siege VARCHAR(20) NOT NULL CHECK (type_siege IN ('FIXE', 'ROTATIF')),
    adresse_siege TEXT,
    montant_cotisation_principale DECIMAL(15,2) NOT NULL,
    frequence_reunion VARCHAR(50) NOT NULL,
    nombre_membres_max INTEGER,
    code_invitation VARCHAR(50) UNIQUE NOT NULL,
    reglement_interieur TEXT,
    statut VARCHAR(50) NOT NULL DEFAULT 'ACTIF',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS groupes.groupe_membre (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL REFERENCES groupes.groupe(id) ON DELETE CASCADE,
    membre_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('CREATEUR','PRESIDENT','TRESORIER','SECRETAIRE','AUDITEUR','MEMBRE')),
    statut VARCHAR(50) NOT NULL DEFAULT 'ACTIF',
    date_adhesion TIMESTAMP NOT NULL DEFAULT NOW(),
    date_fin TIMESTAMP,
    CONSTRAINT uq_groupe_membre UNIQUE (groupe_id, membre_id)
);

CREATE TABLE IF NOT EXISTS groupes.session_tontine (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL REFERENCES groupes.groupe(id) ON DELETE CASCADE,
    libelle VARCHAR(200) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    montant_cagnotte_par_seance DECIMAL(15,2),
    nombre_tours_total INTEGER,
    statut VARCHAR(50) NOT NULL DEFAULT 'PLANIFIEE' CHECK (statut IN ('PLANIFIEE', 'EN_COURS', 'CLOTUREE')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS groupes.mandat_bureau (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL REFERENCES groupes.groupe(id) ON DELETE CASCADE,
    president_membre_id UUID NOT NULL,
    tresorier_membre_id UUID,
    secretaire_membre_id UUID,
    vice_president_membre_id UUID,
    auditeur_membre_id UUID,
    date_debut DATE NOT NULL,
    date_fin DATE,
    actif BOOLEAN NOT NULL DEFAULT true,
    statut VARCHAR(50) NOT NULL DEFAULT 'EN_COURS' CHECK (statut IN ('EN_COURS', 'EXPIRE', 'DESTITUE')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_groupe_createur ON groupes.groupe(createur_membre_id);
CREATE INDEX IF NOT EXISTS idx_groupe_code_invitation ON groupes.groupe(code_invitation);
CREATE INDEX IF NOT EXISTS idx_groupe_membre_groupe ON groupes.groupe_membre(groupe_id);
CREATE INDEX IF NOT EXISTS idx_groupe_membre_membre ON groupes.groupe_membre(membre_id);
CREATE INDEX IF NOT EXISTS idx_session_groupe ON groupes.session_tontine(groupe_id);
CREATE INDEX IF NOT EXISTS idx_mandat_groupe ON groupes.mandat_bureau(groupe_id);
