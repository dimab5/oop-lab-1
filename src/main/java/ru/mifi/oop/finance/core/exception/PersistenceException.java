package ru.mifi.oop.finance.core.exception;

/**
 * A wallet or user file could not be read, validated or written.
 */
public final class PersistenceException extends AppException {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
