package com.njangi.notifications.exception;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(UUID id) {
        super("Notification non trouvée avec l'identifiant : " + id);
    }

    public NotificationNotFoundException(String message) {
        super(message);
    }
}
