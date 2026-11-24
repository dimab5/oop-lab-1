package ru.mifi.oop.finance.cli;

import ru.mifi.oop.finance.core.exception.AppException;

/**
 * Invalid command syntax or command-level problem that should be shown without a stack trace.
 */
public final class CommandException extends AppException {

    public CommandException(String message) {
        super(message);
    }
}
