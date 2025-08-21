package org.univr.telemedicina.exception;

// eccezione personalizzata per errori di accesso ai dati
public class DataAccessException extends Exception {
    // costruttore che accetta un messaggio di errore e una causa
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    // per chiamare il costruttore senza cause
    public DataAccessException(String message) {
        super(message);
    }
}
