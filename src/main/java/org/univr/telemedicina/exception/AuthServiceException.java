package org.univr.telemedicina.exception;

// eccezione personalizzata per errori nel servizio di autenticazione
public class AuthServiceException extends RuntimeException {
    // costruttore che accetta un messaggio di errore e una causa
    public AuthServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}