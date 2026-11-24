-- ====================================================================
-- Initialisation des 4 schémas PostgreSQL isolés — Plateforme Njangi
-- Architecture consolidée (GEMINI.md & ARCHITECTURE_MIGRATION.md)
-- ====================================================================

CREATE SCHEMA IF NOT EXISTS identite;
CREATE SCHEMA IF NOT EXISTS gouvernance;
CREATE SCHEMA IF NOT EXISTS finances;
CREATE SCHEMA IF NOT EXISTS engagement;

-- Droits pour l'utilisateur applicatif
GRANT ALL ON SCHEMA identite TO njangi;
GRANT ALL ON SCHEMA gouvernance TO njangi;
GRANT ALL ON SCHEMA finances TO njangi;
GRANT ALL ON SCHEMA engagement TO njangi;
