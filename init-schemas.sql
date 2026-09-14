-- ====================================================================
-- Initialisation des schémas PostgreSQL pour la plateforme Njangi
-- Conforme à l'architecture multi-schémas isolés (GEMINI.md)
-- ====================================================================

CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS membres;
CREATE SCHEMA IF NOT EXISTS groupes;
CREATE SCHEMA IF NOT EXISTS reunions;
CREATE SCHEMA IF NOT EXISTS cotisations;
CREATE SCHEMA IF NOT EXISTS paiements;
CREATE SCHEMA IF NOT EXISTS penalites;
CREATE SCHEMA IF NOT EXISTS notifications;
CREATE SCHEMA IF NOT EXISTS statistiques;

-- Droits pour l'utilisateur de l'application
GRANT ALL ON SCHEMA auth TO njangi;
GRANT ALL ON SCHEMA membres TO njangi;
GRANT ALL ON SCHEMA groupes TO njangi;
GRANT ALL ON SCHEMA reunions TO njangi;
GRANT ALL ON SCHEMA cotisations TO njangi;
GRANT ALL ON SCHEMA paiements TO njangi;
GRANT ALL ON SCHEMA penalites TO njangi;
GRANT ALL ON SCHEMA notifications TO njangi;
GRANT ALL ON SCHEMA statistiques TO njangi;
