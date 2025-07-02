package org.univr.telemedicina.exception;

public class MedicoNotFound extends RuntimeException {
    public MedicoNotFound(String message) {
        super(message);
    }
    public MedicoNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
