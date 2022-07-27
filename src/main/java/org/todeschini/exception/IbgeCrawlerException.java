package org.todeschini.exception;

public class IbgeCrawlerException extends RuntimeException {

    public IbgeCrawlerException(String message) {
        super(message);
    }

    public IbgeCrawlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
