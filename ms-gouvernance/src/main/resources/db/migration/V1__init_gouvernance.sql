CREATE SCHEMA IF NOT EXISTS gouvernance;

-- ============================================================================
-- 1. GROUPES DE TONTINE, ADHÉSIONS, SESSIONS ET MANDATS DE BUREAU
-- ============================================================================

CREATE TABLE IF NOT EXISTS gouvernance.groupe (
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
    statut VARCHAR(50) NOT NULL DEFAULT 'ACTIF' CHECK (statut IN ('ACTIF', 'SUSPENDU', 'CLOTURE')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS gouvernance.groupe_membre (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL REFERENCES gouvernance.groupe(id) ON DELETE CASCADE,
    membre_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('CREATEUR', 'PRESIDENT', 'TRESORIER', 'SECRETAIRE', 'AUDITEUR', 'MEMBRE')),
    statut VARCHAR(50) NOT NULL DEFAULT 'ACTIF' CHECK (statut IN ('ACTIF', 'SUSPENDU', 'RADIE', 'DEMISSIONNAIRE')),
    date_adhesion TIMESTAMP NOT NULL DEFAULT NOW(),
    date_fin TIMESTAMP,
    CONSTRAINT uq_groupe_membre UNIQUE (groupe_id, membre_id)
);

CREATE TABLE IF NOT EXISTS gouvernance.session_tontine (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL REFERENCES gouvernance.groupe(id) ON DELETE CASCADE,
    libelle VARCHAR(200) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    montant_cagnotte_par_seance DECIMAL(15,2),
    nombre_tours_total INTEGER,
    statut VARCHAR(50) NOT NULL DEFAULT 'PLANIFIEE' CHECK (statut IN ('PLANIFIEE', 'EN_COURS', 'CLOTUREE')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS gouvernance.mandat_bureau (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL REFERENCES gouvernance.groupe(id) ON DELETE CASCADE,
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

-- ============================================================================
-- 2. RÉUNIONS EN DIRECT & REGISTRE D'ÉMARGEMENT DES PRÉSENCES
-- ============================================================================

CREATE TABLE IF NOT EXISTS gouvernance.reunion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    groupe_id UUID NOT NULL REFERENCES gouvernance.groupe(id) ON DELETE CASCADE,
    session_tontine_id UUID REFERENCES gouvernance.session_tontine(id) ON DELETE SET NULL,
    titre VARCHAR(300) NOT NULL,
    date_reunion TIMESTAMP NOT NULL,
    lieu_reunion TEXT,
    type_siege VARCHAR(20) NOT NULL DEFAULT 'FIXE' CHECK (type_siege IN ('FIXE', 'ROTATIF')),
    hote_id UUID,
    statut VARCHAR(50) NOT NULL DEFAULT 'PLANIFIEE' CHECK (statut IN ('PLANIFIEE', 'EN_COURS', 'TERMINEE', 'ANNULEE')),
    ordre_jour TEXT,
    compte_rendu TEXT,
    president_reunion_id UUID,
    secretaire_reunion_id UUID,
    tresorier_reunion_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS gouvernance.presence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reunion_id UUID NOT NULL REFERENCES gouvernance.reunion(id) ON DELETE CASCADE,
    membre_id UUID NOT NULL,
    statut VARCHAR(30) NOT NULL DEFAULT 'ABSENT' CHECK (statut IN ('PRESENT', 'RETARD', 'EXCUSE', 'ABSENT')),
    heure_arrivee TIMESTAMP,
    justification TEXT,
    procuration BOOLEAN NOT NULL DEFAULT false,
    mandataire_id UUID,
    enregistre_le TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_reunion_membre UNIQUE (reunion_id, membre_id)
);

-- ============================================================================
-- 3. INDEX
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_groupe_createur ON gouvernance.groupe(createur_membre_id);
CREATE INDEX IF NOT EXISTS idx_groupe_code_invitation ON gouvernance.groupe(code_invitation);
CREATE INDEX IF NOT EXISTS idx_groupe_membre_groupe ON gouvernance.groupe_membre(groupe_id);
CREATE INDEX IF NOT EXISTS idx_groupe_membre_membre ON gouvernance.groupe_membre(membre_id);
CREATE INDEX IF NOT EXISTS idx_session_groupe ON gouvernance.session_tontine(groupe_id);
CREATE INDEX IF NOT EXISTS idx_mandat_groupe ON gouvernance.mandat_bureau(groupe_id);
CREATE INDEX IF NOT EXISTS idx_reunion_groupe ON gouvernance.reunion(groupe_id);
CREATE INDEX IF NOT EXISTS idx_reunion_session ON gouvernance.reunion(session_tontine_id);
CREATE INDEX IF NOT EXISTS idx_reunion_statut ON gouvernance.reunion(statut);
CREATE INDEX IF NOT EXISTS idx_reunion_date ON gouvernance.reunion(date_reunion);
CREATE INDEX IF NOT EXISTS idx_presence_reunion ON gouvernance.presence(reunion_id);
CREATE INDEX IF NOT EXISTS idx_presence_membre ON gouvernance.presence(membre_id);
