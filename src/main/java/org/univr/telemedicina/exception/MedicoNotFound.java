package org.univr.telemedicina.exception;

// eccezione personalizzata per quando un medico non viene trovato
public class MedicoNotFound extends RuntimeException {
    // costruttore che accetta un messaggio di errore
    public MedicoNotFound(String message) {
        super(message);
    }
    // costruttore che accetta un messaggio di errore e una causa
    public MedicoNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
