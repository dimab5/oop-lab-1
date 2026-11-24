package ru.mifi.oop.finance.core.service;

/**
 * Importance of a notification shown after an operation.
 */
public enum NotificationLevel {
    INFO("i"),
    WARNING("!"),
    DANGER("!!");

    private final String marker;

    NotificationLevel(String marker) {
        this.marker = marker;
    }

    public String marker() {
        return marker;
    }
}
