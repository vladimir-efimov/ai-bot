package ru.javaops.ai_bot;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.javaops.ai_bot.ai.CourseAdvertiser;
import ru.javaops.ai_bot.handler.ClientHandler;
import ru.javaops.ai_bot.handler.CommandHandler;
import ru.javaops.ai_bot.handler.KeyboardHandler;
import ru.javaops.ai_bot.handler.UpdateHandler;

import java.io.IOException;

import static ru.javaops.ai_bot.AIBot.Stage.HANDLE_BASE_PROGRAM_QUESTION;
import static ru.javaops.ai_bot.AIBot.Stage.HANDLE_BASE_TEST_QUESTION;
import static ru.javaops.ai_bot.AIBot.Stage.HANDLE_SYNTAX_QUESTION;
import static ru.javaops.ai_bot.AIBot.Stage.HANDLE_TOP_PROGRAM_QUESTION;
import static ru.javaops.ai_bot.AIBot.Stage.HANDLE_TOP_TEST_QUESTION;

import static ru.javaops.ai_bot.handler.KeyboardHandler.createInlineButton;

@Slf4j
@Component
public class AIBot implements LongPollingSingleThreadUpdateConsumer {

    private static final String START_JAVA_COURSE = "Java для начинающих: [StartJava](https://javaops.ru/view/startjava?ref=aibot)";
    private static final String BASE_JAVA_COURSE = "Web Java разработчик: [BaseJava](https://javaops.ru/view/basejava?ref=aibot)";
    private static final String AI_BOT_COURSE = "Telegram бот курс: [AI-Bot](https://javaops.ru/view/ai-bot?ref=aibot)";
    private static final String TOP_JAVA_COURSE = "Enterprise Java разработчик: [TopJava](https://javaops.ru/view/topjava?ref=aibot)";
    private static final String CLOUD_JAVA_COURSE = "Курс для уровней Middle и Senior: [CloudJava](https://javaops.ru/view/cloudjava?ref=aibot)";
    private static final String DEV_OPS_COURSE = "Dev Ops курс: [Deploy microservices to Kubernetes. Helm](https://javaops.ru/view/cloudjava2?ref=aibot)";

    private static final String CHOOSE_DIRECTION_MSG = "Выберите направление обучения";

    @Value("${ai-bot.telegram.token}")
    @Getter
    private String token;

    @Value("${ai-bot.yandex-cloud.gateway}")
    private String yandexCloudGateway;

    protected CourseAdvertiser courseAdvertiser;
    protected ClientHandler clientHandler;
    protected final States<Stage> states = new States<>();

    public enum Stage {
        HANDLE_SYNTAX_QUESTION,
        HANDLE_BASE_PROGRAM_QUESTION,
        HANDLE_BASE_TEST_QUESTION,
        HANDLE_TOP_TEST_QUESTION,
        HANDLE_TOP_PROGRAM_QUESTION,
    }


    @PostConstruct
    public void init() {
        clientHandler = new ClientHandler(token);
        courseAdvertiser = new CourseAdvertiser(yandexCloudGateway);
    }

    @Override
    public void consume(Update update) {
        long tgId = UpdateHandler.getFrom(update).getId();
        Stage state = states.getCurrent(tgId);
        log.info("Update with state {} received from {}", state, tgId);
        Message msg = UpdateHandler.getMessage(update);
        if (CommandHandler.isHelp(msg)) {
            clientHandler.sendMd(tgId, """
                    Успехов в обучении и карьере Java разработчика! Посмотрите на
                    💥 [JavaOPs план обучения](https://javaops.ru/view/roadmap?ref=aibot)
                    💥 [Тест на знание Java, общий и по темам](https://t.me/JavaOPsTestBot)
                    💥 [Материалы для подготовки](https://javaops.ru/view/test?ref=aibot)
                    """);
        } else if (CommandHandler.isStart(msg) || state == null) {
            sendYesNo(tgId, """
                    Пройдите короткий тест для определения подходящего Вам курса.
                    Вы знаете синтаксис Java?
                    """, HANDLE_SYNTAX_QUESTION);
        } else {
            switch (state) {
                case HANDLE_SYNTAX_QUESTION -> UpdateHandler.treatNoAndYes(update,
                        () -> finish(tgId, START_JAVA_COURSE),
                        () -> sendChooseDirectionQuestion(tgId, "Java Web / enterprise", "AI и чат-боты",
                                HANDLE_BASE_PROGRAM_QUESTION)
                );
                case HANDLE_BASE_PROGRAM_QUESTION -> UpdateHandler.treat2OptionsQuestion(update,
                        () -> sendYesNo(tgId, "Вы знаете Java Core, JDBC и Servlets?\nВы проходили [BaseJava program](https://javaops.ru/view/basejava#program)?",
                                HANDLE_BASE_TEST_QUESTION),
                        () -> finish(tgId, AI_BOT_COURSE),
                        "Java Web / enterprise"
                );
                case HANDLE_BASE_TEST_QUESTION -> UpdateHandler.treatNoAndYes(update,
                        () -> finish(tgId, BASE_JAVA_COURSE),
                        () -> sendYesNo(tgId, "Вы знаете Maven, Spring, JPA, REST?\nВы проходили [TopJava program](https://javaops.ru/view/topjava#schedule)?",
                                HANDLE_TOP_TEST_QUESTION)
                );
                case HANDLE_TOP_TEST_QUESTION -> UpdateHandler.treatNoAndYes(update,
                        () -> finish(tgId, TOP_JAVA_COURSE),
                        () -> sendChooseDirectionQuestion(tgId, "Java enterprise", "DevOps",
                                HANDLE_TOP_PROGRAM_QUESTION)
                );
                case HANDLE_TOP_PROGRAM_QUESTION -> UpdateHandler.treat2OptionsQuestion(update,
                        () -> finish(tgId, CLOUD_JAVA_COURSE),
                        () -> finish(tgId, DEV_OPS_COURSE),
                        "Java enterprise"
                );
            }
        }
    }

    private void sendYesNo(long tgId, String msg, Stage nextStage) {
        clientHandler.sendMdAndKeyboard(tgId, msg, UpdateHandler.YES_NO_KEYBOARD);
        states.update(tgId, nextStage);
    }

    private void sendChooseDirectionQuestion(long tgId, String option1, String option2, Stage nextStage) {
        InlineKeyboardMarkup directionKeys = KeyboardHandler.createSingleRowMarkup(
                createInlineButton(option1, option1),
                createInlineButton(option2, option2));
        clientHandler.sendMdAndKeyboard(tgId, CHOOSE_DIRECTION_MSG, directionKeys);
        states.update(tgId, nextStage);
    }

    private void finish(long tgId, String course) {
        try {
            courseAdvertiser.advertise(tgId, course);
        } catch (IOException ex) {
            log.error(ex.getMessage());
            // fallback to simple recommendation without AI service usage
            clientHandler.sendMd(tgId, "Посмотрите на курс " + course);
        }
        states.invalidate(tgId);
    }
}
