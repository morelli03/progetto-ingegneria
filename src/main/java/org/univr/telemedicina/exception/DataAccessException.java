package org.univr.telemedicina.exception;

public class DataAccessException extends Exception {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    // per chiamare il costruttore senza cause.
    public DataAccessException(String message) {
        super(message);
    }
}
