package ru.mifi.oop.finance.cli;

import java.util.List;

/**
 * One CLI command. Implementations contain presentation and argument parsing only;
 * business rules live in services and domain objects.
 */
public interface Command {

    String name();

    String usage();

    String summary();

    default List<String> examples() {
        return List.of();
    }

    default boolean requiresAuthentication() {
        return true;
    }

    void execute(CommandContext context, List<String> args);
}
