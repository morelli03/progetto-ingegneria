package org.univr.telemedicina.exception;

// eccezione personalizzata per errori relativi alla terapia
public class TherapyException extends Exception {
    // costruttore che accetta un messaggio di errore
    public TherapyException(String message) {
        super(message);
    }
    // costruttore che accetta un messaggio di errore e una causa
    public TherapyException(String message, Throwable cause) {
        super(message, cause);
    }
}
