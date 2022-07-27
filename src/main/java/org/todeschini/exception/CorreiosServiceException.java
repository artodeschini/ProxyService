package org.todeschini.exception;

public class CorreiosServiceException extends RuntimeException {

    public CorreiosServiceException(String message) {
        super(message);
    }

    public CorreiosServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
