package org.univr.telemedicina.exception;

public class TherapyException extends Exception {
    public TherapyException(String message) {
        super(message);
    }
    public TherapyException(String message, Throwable cause) {
        super(message, cause);
    }
}
