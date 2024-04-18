package org.todeschini.exception;

public class ConverterMoedaException extends RuntimeException {
    public ConverterMoedaException() {
    }

    public ConverterMoedaException(String message) {
        super(message);
    }

    public ConverterMoedaException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConverterMoedaException(Throwable cause) {
        super(cause);
    }

    public ConverterMoedaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
