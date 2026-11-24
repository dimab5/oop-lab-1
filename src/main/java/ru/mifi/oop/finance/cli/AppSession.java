package ru.mifi.oop.finance.cli;

import ru.mifi.oop.finance.core.auth.UserAccount;
import ru.mifi.oop.finance.core.model.Wallet;

/**
 * Currently authenticated user and their loaded wallet. Only one user can be logged in
 * at a time, which matches the interactive CLI model.
 */
public final class AppSession {

    private UserAccount user;
    private Wallet wallet;

    public boolean isAuthenticated() {
        return user != null && wallet != null;
    }

    public void start(UserAccount account, Wallet loadedWallet) {
        if (isAuthenticated()) {
            throw new CommandException("Сначала завершите текущую сессию командой logout.");
        }
        this.user = account;
        this.wallet = loadedWallet;
    }

    public void end() {
        this.user = null;
        this.wallet = null;
    }

    public UserAccount requireUser() {
        if (user == null) {
            throw new CommandException("Сначала выполните вход: login <логин> <пароль>.");
        }
        return user;
    }

    public Wallet requireWallet() {
        if (wallet == null) {
            throw new CommandException("Сначала выполните вход: login <логин> <пароль>.");
        }
        return wallet;
    }

    public void replaceWallet(Wallet replacement) {
        if (!isAuthenticated()) {
            throw new CommandException("Сначала выполните вход.");
        }
        this.wallet = replacement;
    }

    public String prompt() {
        return isAuthenticated() ? user.login() + "> " : "> ";
    }
}
