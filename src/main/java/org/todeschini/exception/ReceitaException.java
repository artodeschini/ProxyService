package org.todeschini.exception;

public class ReceitaException extends RuntimeException {

    public ReceitaException(String message) {
        super(message);
    }

    public ReceitaException(String message, Throwable cause) {
        super(message, cause);
    }
}
