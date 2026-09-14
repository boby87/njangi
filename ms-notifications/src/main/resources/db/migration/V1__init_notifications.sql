-- Migration V1 : Initialisation du schéma notifications
-- Microservice ms-notifications — Application Njangi Tontine

CREATE SCHEMA IF NOT EXISTS notifications;

CREATE TABLE notifications.notification (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    destinataire_id      UUID         NOT NULL,
    groupe_id            UUID,
    canal                VARCHAR(10)  NOT NULL CHECK (canal IN ('PUSH', 'SMS', 'EMAIL', 'IN_APP')),
    type                 VARCHAR(100) NOT NULL,
    titre                VARCHAR(255) NOT NULL,
    contenu              TEXT         NOT NULL,
    statut               VARCHAR(15)  NOT NULL DEFAULT 'EN_ATTENTE'
                                      CHECK (statut IN ('EN_ATTENTE', 'ENVOYEE', 'ECHEC')),
    lue                  BOOLEAN      NOT NULL DEFAULT FALSE,
    cree_le              TIMESTAMP    NOT NULL DEFAULT NOW(),
    envoyee_le           TIMESTAMP,
    reference_objet      VARCHAR(100),
    type_objet           VARCHAR(50),
    destinataire_contact VARCHAR(255)
);

-- Index sur les colonnes de recherche fréquente
CREATE INDEX idx_notif_dest_id   ON notifications.notification (destinataire_id);
CREATE INDEX idx_notif_statut    ON notifications.notification (statut);
CREATE INDEX idx_notif_groupe_id ON notifications.notification (groupe_id);
CREATE INDEX idx_notif_lue       ON notifications.notification (lue);
CREATE INDEX idx_notif_dest_lue  ON notifications.notification (destinataire_id, lue);

COMMENT ON TABLE  notifications.notification               IS 'Notifications envoyées aux membres Njangi (Push, SMS, Email, In-App)';
COMMENT ON COLUMN notifications.notification.canal         IS 'PUSH | SMS | EMAIL | IN_APP';
COMMENT ON COLUMN notifications.notification.statut        IS 'EN_ATTENTE | ENVOYEE | ECHEC';
COMMENT ON COLUMN notifications.notification.destinataire_id IS 'UUID du membre destinataire';
