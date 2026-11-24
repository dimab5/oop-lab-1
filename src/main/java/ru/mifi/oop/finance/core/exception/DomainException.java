package ru.mifi.oop.finance.core.exception;

/**
 * A business rule of the finance domain was violated.
 */
public final class DomainException extends AppException {

    public DomainException(String message) {
        super(message);
    }
}
