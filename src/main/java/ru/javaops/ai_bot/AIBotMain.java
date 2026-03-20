package ru.javaops.ai_bot;

import org.slf4j.Logger;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

public class AIBotMain {

    private static final Logger log = getLogger(AIBotMain.class);

    public static void main(String[] args) throws Exception {
        String botToken = Objects.requireNonNull(System.getenv("BOT_HTTP_API_TOKEN"), "Set BOT_HTTP_API_TOKEN environment variable"); //"BOT_HTTP_API_Token";
        // https://rubenlagus.github.io/TelegramBotsDocumentation/getting-started.html
        try (TelegramBotsLongPollingApplication botsApp = new TelegramBotsLongPollingApplication()) {
            botsApp.registerBot(botToken, new AIBot(botToken));
            log.info("Bot registered");
            Thread.currentThread().join();  // wait
        }
    }
}
