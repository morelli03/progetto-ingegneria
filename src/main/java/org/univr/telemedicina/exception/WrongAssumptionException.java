package org.univr.telemedicina.exception;

// eccezione personalizzata per un'assunzione errata
public class WrongAssumptionException extends Exception {
    // costruttore che accetta un messaggio di errore
    public WrongAssumptionException(String message) {
        super(message);
    }
}
