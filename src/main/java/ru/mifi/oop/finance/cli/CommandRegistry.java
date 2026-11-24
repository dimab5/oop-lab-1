package ru.mifi.oop.finance.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Command lookup and "did you mean" suggestions for typos.
 */
public final class CommandRegistry {

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Map<String, String> aliases = new LinkedHashMap<>();

    public void register(Command command) {
        String name = command.name();
        if (commands.containsKey(name) || commands.containsValue(command)) {
            throw new IllegalArgumentException("Команда «" + name + "» уже зарегистрирована.");
        }
        commands.put(name, command);
    }

    public void alias(String alias, String commandName) {
        if (commands.containsKey(alias) || aliases.containsKey(alias)) {
            throw new IllegalArgumentException("Имя «" + alias + "» уже занято.");
        }
        if (!commands.containsKey(commandName)) {
            throw new IllegalArgumentException("Неизвестная команда для псевдонима: " + commandName);
        }
        aliases.put(alias, commandName);
    }

    public Optional<Command> find(String name) {
        Command command = commands.get(name);
        if (command != null) {
            return Optional.of(command);
        }
        String target = aliases.get(name);
        return target == null ? Optional.empty() : Optional.of(commands.get(target));
    }

    public List<Command> all() {
        return List.copyOf(commands.values());
    }

    public String suggestion(String unknown) {
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        List<String> candidates = new ArrayList<>(commands.keySet());
        candidates.addAll(aliases.keySet());
        for (String candidate : candidates) {
            int distance = distance(unknown, candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return bestDistance <= 2 ? best : null;
    }

    private static int distance(String left, String right) {
        int[][] matrix = new int[left.length() + 1][right.length() + 1];
        for (int row = 0; row <= left.length(); row++) {
            matrix[row][0] = row;
        }
        for (int column = 0; column <= right.length(); column++) {
            matrix[0][column] = column;
        }
        for (int row = 1; row <= left.length(); row++) {
            for (int column = 1; column <= right.length(); column++) {
                int cost = left.charAt(row - 1) == right.charAt(column - 1) ? 0 : 1;
                matrix[row][column] = Math.min(
                        Math.min(matrix[row - 1][column] + 1, matrix[row][column - 1] + 1),
                        matrix[row - 1][column - 1] + cost);
            }
        }
        return matrix[left.length()][right.length()];
    }
}
