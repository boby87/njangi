package com.njangi.statistiques.exception;

import java.util.UUID;

public class StatistiqueNotFoundException extends RuntimeException {

    public StatistiqueNotFoundException(UUID groupeId, UUID sessionId) {
        super("Statistique non trouvée pour groupe=" + groupeId + " session=" + sessionId);
    }

    public StatistiqueNotFoundException(String message) {
        super(message);
    }
}
