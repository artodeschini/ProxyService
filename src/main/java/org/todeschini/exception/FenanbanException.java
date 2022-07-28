package org.todeschini.exception;

public class FenanbanException extends RuntimeException {

    public FenanbanException(String message) {
        super(message);
    }

    public FenanbanException(String message, Throwable cause) {
        super(message, cause);
    }
}
