package org.univr.telemedicina.exception;

public class MedicoServiceException extends Exception {
    public MedicoServiceException(String message) {
        super(message);
    }

    public MedicoServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
