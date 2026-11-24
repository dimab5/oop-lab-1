package ru.mifi.oop.finance.cli;

import java.util.List;

/**
 * Shared parsing of the optional {@code --note} argument.
 */
public final class CommandArguments {

    private CommandArguments() {
    }

    public static String noteFrom(List<String> args, int startIndex) {
        if (startIndex >= args.size()) {
            return "";
        }
        String option = args.get(startIndex);
        if (!"--note".equals(option)) {
            throw new CommandException("Неизвестный параметр «" + option + "». Ожидается --note <текст>.");
        }
        if (startIndex + 1 >= args.size()) {
            throw new CommandException("После --note укажите текст комментария.");
        }
        String note = String.join(" ", args.subList(startIndex + 1, args.size())).strip();
        if (note.isEmpty()) {
            throw new CommandException("Комментарий не может быть пустым.");
        }
        return note;
    }
}
