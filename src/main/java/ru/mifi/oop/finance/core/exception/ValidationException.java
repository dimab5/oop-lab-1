package ru.mifi.oop.finance.core.exception;

/**
 * Invalid user input (amount, login, category, command arguments, and so on).
 */
public final class ValidationException extends AppException {

    public ValidationException(String message) {
        super(message);
    }
}
