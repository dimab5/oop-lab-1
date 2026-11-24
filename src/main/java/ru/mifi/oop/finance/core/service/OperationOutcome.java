package ru.mifi.oop.finance.core.service;

import java.util.List;

import ru.mifi.oop.finance.core.model.OperationResult;

/**
 * Added operation plus any threshold notifications caused by it.
 */
public record OperationOutcome(OperationResult result, List<Notification> notifications) {

    public OperationOutcome {
        notifications = List.copyOf(notifications);
    }
}
