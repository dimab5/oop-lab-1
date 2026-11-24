package ru.mifi.oop.finance.core.validation;

import java.util.Locale;
import java.util.regex.Pattern;

import ru.mifi.oop.finance.core.exception.ValidationException;

/**
 * Centralized validation of user-facing strings. All messages are safe to show in the CLI.
 */
public final class Validators {

    public static final int LOGIN_MIN_LENGTH = 3;
    public static final int LOGIN_MAX_LENGTH = 30;
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 128;
    public static final int CATEGORY_MAX_LENGTH = 40;
    public static final int NOTE_MAX_LENGTH = 200;

    private static final Pattern LOGIN_PATTERN = Pattern.compile("[\\p{L}\\p{N}_-]+");

    private Validators() {
    }

    /**
     * Normalizes a login to lower case, so logins are case-insensitive.
     */
    public static String login(String raw) {
        String value = requireText(raw, "Логин");
        value = value.toLowerCase(Locale.ROOT);
        if (value.length() < LOGIN_MIN_LENGTH || value.length() > LOGIN_MAX_LENGTH) {
            throw new ValidationException("Логин должен содержать от " + LOGIN_MIN_LENGTH
                    + " до " + LOGIN_MAX_LENGTH + " символов.");
        }
        if (!LOGIN_PATTERN.matcher(value).matches()) {
            throw new ValidationException("Логин может содержать буквы, цифры, «_» и «-».");
        }
        return value;
    }

    public static String password(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Пароль не может быть пустым.");
        }
        if (raw.length() < PASSWORD_MIN_LENGTH) {
            throw new ValidationException("Пароль должен содержать минимум " + PASSWORD_MIN_LENGTH + " символов.");
        }
        if (raw.length() > PASSWORD_MAX_LENGTH) {
            throw new ValidationException("Пароль должен содержать не более " + PASSWORD_MAX_LENGTH + " символов.");
        }
        return raw;
    }

    public static String categoryName(String raw) {
        String value = requireText(raw, "Название категории");
        if (value.length() > CATEGORY_MAX_LENGTH) {
            throw new ValidationException("Название категории должно содержать не более "
                    + CATEGORY_MAX_LENGTH + " символов.");
        }
        if (value.indexOf(',') >= 0) {
            throw new ValidationException("Название категории не может содержать запятую.");
        }
        for (int index = 0; index < value.length(); index++) {
            if (Character.isISOControl(value.charAt(index))) {
                throw new ValidationException("Название категории содержит недопустимый управляющий символ.");
            }
        }
        return value;
    }

    public static String note(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.strip();
        if (value.length() > NOTE_MAX_LENGTH) {
            throw new ValidationException("Комментарий должен содержать не более " + NOTE_MAX_LENGTH + " символов.");
        }
        return value;
    }

    public static String requireText(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException(field + " не может быть пустым.");
        }
        return raw.strip();
    }
}
