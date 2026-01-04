package sugar.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import sugar.telegram.loger.Logger;
import sugar.telegram.notification.Notification;
import sugar.telegram.state.UserSt;
import sugar.telegram.util.file.FileWriter;
import sugar.telegram.util.message.Message;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@RequiredArgsConstructor
@Slf4j
@Component
public class CommandFilter {

    private final Message message;
    private final FileWriter writer;
    private final Notification notification;
    private static final File file = new File("logger.txt");
    private Set<Long> setChatId = new HashSet<>();
    private final Map<Long, UserSt> userStMap = new TreeMap<>();

    public void command(Update update) {

        if (update.hasMessage()) {

            if (update.getMessage().hasText()) {

                Logger logger = new Logger(update.getMessage().getChatId(), update.getMessage().getText());
                setChatId.add(logger.getChatId());
                log.info("Новое сообщение: {}", logger);

                notification.sendNotification(setChatId, update.getMessage().getFrom().getFirstName(), message);

                writer.fileWriter(logger, file);

                UserSt userSt = userStMap.get(logger.getChatId());

                if (userSt != null) {

                    switch (userSt.getState()) {
                        case SUGAR -> handleWriteSugar(logger.getChatId(), logger.getMessage());

                        case INSULIN -> handleWriteInsulin(logger.getChatId(), logger.getMessage());

                        case NOTE -> handleWriteNote(logger.getChatId(), logger.getMessage());

                        case REMOVE -> removeById(logger.getChatId(), logger.getMessage());

                        case GET_SUGAR_BY_ID -> getSugarById(logger.getChatId(), logger.getMessage());

                        case WAIT_ID_FOR_UPDATE -> sugarUpdate(logger.getChatId(), logger.getMessage(), userSt);

                        case UPDATE_START -> begin(logger.getChatId(), logger.getMessage());

                        case WAIT_SUGAR_FOR_UPDATE -> handleWriteUpdateSugar(logger.getChatId(), logger.getMessage());

                        case WAIT_INSULIN_FOR_UPDATE ->
                                handleWriteUpdateInsulin(logger.getChatId(), logger.getMessage());

                        case WAIT_NOTE_FOR_UPDATE -> handleWriteUpdateNote(logger.getChatId(), logger.getMessage());
                    }

                } else if (logger.getMessage().equals("/start")) {
                    sendMenu(logger.getChatId());

                } else if (logger.getMessage().equals("/help")) {
                    message.execute(message.sendMessage(logger.getChatId(), "Скоро здесь появится инструкция"));

                } else {
                    message.execute(message.sendMessage(logger.getChatId(), "click 👉 /start"));
                }
            } else {
                message.execute(message.sendMessage(update.getMessage().getChatId(), "Я еще в мастерской 🥲"));
            }

        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getFrom().getId();

            log.info("{} нажал кнопку: {}", chatId, data);

            switch (data) {
                case "addEntry" -> start(chatId);

                case "updateEntry" -> updateStart(chatId);

                case "getById" -> getBySugarIdStart(chatId);

                case "removeById" -> removeStart(chatId);

                default -> message.execute(message.sendMessage(chatId, "Не известная команда 🤷‍♂️"));
            }
        }
    }

    private void sendMenu(Long chatId) {
        SendMessage sendMessage = message.sendMessage(chatId, "Вот что я могу:");

        InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                .text("Создать запись")
                .callbackData("addEntry")
                .build();

        InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                .text("Обновить запись")
                .callbackData("updateEntry")
                .build();

        InlineKeyboardButton button3 = InlineKeyboardButton.builder()
                .text("Получить запись по ID")
                .callbackData("getById")
                .build();

        InlineKeyboardButton button4 = InlineKeyboardButton.builder()
                .text("Удалить запись")
                .callbackData("removeById")
                .build();

        List<InlineKeyboardRow> keyboards = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3),
                new InlineKeyboardRow(button4)
        );

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(keyboards);

        sendMessage.setReplyMarkup(keyboardMarkup);

        message.execute(sendMessage);
    }
}
