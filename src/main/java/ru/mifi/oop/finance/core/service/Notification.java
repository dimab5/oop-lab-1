package ru.mifi.oop.finance.core.service;

import java.util.Objects;

/**
 * A user-facing message produced by business events.
 */
public record Notification(NotificationLevel level, String message) {

    public Notification {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(message, "message");
    }

    public static Notification info(String message) {
        return new Notification(NotificationLevel.INFO, message);
    }

    public static Notification warning(String message) {
        return new Notification(NotificationLevel.WARNING, message);
    }

    public static Notification danger(String message) {
        return new Notification(NotificationLevel.DANGER, message);
    }
}
