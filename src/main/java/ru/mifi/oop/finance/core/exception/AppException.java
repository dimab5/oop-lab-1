package ru.mifi.oop.finance.core.exception;

/**
 * Base class for expected application errors that can be shown to the user as-is.
 */
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
