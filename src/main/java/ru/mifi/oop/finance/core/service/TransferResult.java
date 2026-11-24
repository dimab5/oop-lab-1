package ru.mifi.oop.finance.core.service;

import java.math.BigDecimal;
import java.util.List;

import ru.mifi.oop.finance.core.model.Transaction;

/**
 * Both sides of a completed wallet-to-wallet transfer.
 */
public record TransferResult(
        String recipientLogin,
        BigDecimal amount,
        Transaction outgoing,
        Transaction incoming,
        BigDecimal senderBalance,
        List<Notification> notifications) {

    public TransferResult {
        notifications = List.copyOf(notifications);
    }
}
