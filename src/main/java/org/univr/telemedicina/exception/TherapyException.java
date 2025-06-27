package org.univr.telemedicina.exception;

public class TherapyException extends RuntimeException {
    public TherapyException(String message) {
        super(message);
    }
    public TherapyException(String message, Throwable cause) {
        super(message, cause);
    }
}
