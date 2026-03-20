package ru.javaops.ai_bot.util;

import ru.javaops.ai_bot.error.TelegramException;

public class Util {

    private Util() {}

    public static <T> T notNull(T value, String msg) {
        if (value == null) throw new TelegramException(msg);
        return value;
    }
}