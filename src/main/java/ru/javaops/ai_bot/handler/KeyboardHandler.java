package ru.javaops.ai_bot.handler;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

public class KeyboardHandler {

    private KeyboardHandler() {}

    public static InlineKeyboardButton createInlineButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    public static InlineKeyboardButton createInlineLinkButton(String text, String url) {
        return InlineKeyboardButton.builder()
                .text(text)
                .url(url)
                .build();
    }

    public static InlineKeyboardMarkup createSingleRowMarkup(InlineKeyboardButton... buttons) {
        return createSingleRowMarkup(List.of(buttons));
    }

    public static InlineKeyboardMarkup createSingleRowMarkup(List<InlineKeyboardButton> listButtons) {
        return createMarkup(new InlineKeyboardRow(listButtons));
    }

    public static InlineKeyboardMarkup createMarkup(InlineKeyboardRow... rows) {
        return createMarkup(List.of(rows));
    }

    public static InlineKeyboardMarkup createMarkup(List<InlineKeyboardRow> listRows) {
        return InlineKeyboardMarkup.builder().keyboard(listRows).build();
    }
}
