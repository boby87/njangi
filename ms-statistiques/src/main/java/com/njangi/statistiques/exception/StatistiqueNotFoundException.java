package com.njangi.statistiques.exception;

import java.util.UUID;

public class StatistiqueNotFoundException extends RuntimeException {
    public StatistiqueNotFoundException(UUID groupeId, UUID sessionId) {
        super("Aucune statistique trouvée pour le groupe " + groupeId + " et la session " + sessionId);
    }

    public StatistiqueNotFoundException(String message) {
        super(message);
    }
}
