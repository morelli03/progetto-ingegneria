package org.univr.telemedicina.exception;

// eccezione personalizzata per errori nel servizio medico
public class MedicoServiceException extends Exception {
    // costruttore che accetta un messaggio di errore
    public MedicoServiceException(String message) {
        super(message);
    }

    // costruttore che accetta un messaggio di errore e una causa
    public MedicoServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
