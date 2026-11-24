package ru.mifi.oop.finance.cli;

/**
 * Loop control shared by commands such as exit.
 */
public final class ApplicationState {

    private boolean exitRequested;

    public boolean isExitRequested() {
        return exitRequested;
    }

    public void requestExit() {
        this.exitRequested = true;
    }
}
