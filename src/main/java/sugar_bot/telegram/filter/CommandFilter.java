package sugar_bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import sugar_bot.sugar.interfaces.SugarService;
import sugar_bot.telegram.addEntry.AddEntry;
import sugar_bot.telegram.clear.Delete;
import sugar_bot.telegram.get.getById.GetById;
import sugar_bot.telegram.get.getForPeriod.GetForPeriod;
import sugar_bot.telegram.loger.Logger;
import sugar_bot.telegram.menu.Menu;
import sugar_bot.telegram.notification.Notification;
import sugar_bot.telegram.state.UserSt;
import sugar_bot.telegram.update.UpdateEntry;
import sugar_bot.telegram.util.file.FileWriter;
import sugar_bot.telegram.util.message.Message;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

@RequiredArgsConstructor
@Slf4j
@Component
public class CommandFilter implements LongPollingSingleThreadUpdateConsumer {

    private final Menu menu;
    private final Message message;
    private final FileWriter writer;
    private final AddEntry addEntry;
    private final Delete delete;
    private final GetById getById;
    private final UpdateEntry updateEntry;
    private final Notification notification;
    private final SugarService sugarService;
    private final GetForPeriod getForPeriod;
    private static final File file = new File("logger.txt");
    private final Map<Long, UserSt> userStMap = new TreeMap<>();

    @Override
    public void consume(Update update) {

        if (update.hasMessage()) {

            if (update.getMessage().hasText()) {

                Logger logger = new Logger(update.getMessage().getChatId(), update.getMessage().getText());
                log.info("Новое сообщение: {}", logger);

                writer.fileWriter(logger, file);

                UserSt userSt = userStMap.get(logger.getChatId());

                if (userSt != null) {

                    switch (userSt.getState()) {

                        case START -> menu.sendMenu(logger.getChatId(), message);

                        case SUGAR ->
                                addEntry.handleWriteSugar(logger.getChatId(), logger.getMessage(), userStMap, message);

                        case INSULIN ->
                                addEntry.handleWriteInsulin(logger.getChatId(), logger.getMessage(), userStMap, message);

                        case NOTE ->
                                addEntry.handleWriteNote(logger.getChatId(), logger.getMessage(), userStMap, message, menu, sugarService);

                        case REMOVE ->
                                delete.removeById(logger.getChatId(), logger.getMessage(), sugarService, message, menu, userStMap);

                        case GET_SUGAR_BY_ID ->
                                getById.getSugarById(logger.getChatId(), logger.getMessage(), sugarService, message, menu, userStMap);

                        case WAIT_ID_FOR_UPDATE ->
                                updateEntry.sugarUpdate(logger.getChatId(), logger.getMessage(), userSt, sugarService, message, userStMap);

                        case UPDATE_START ->
                                updateEntry.begin(logger.getChatId(), logger.getMessage(), userStMap, message, menu, sugarService);

                        case WAIT_SUGAR_FOR_UPDATE ->
                                updateEntry.handleWriteUpdateSugar(logger.getChatId(), logger.getMessage(), userStMap, message);

                        case WAIT_INSULIN_FOR_UPDATE ->
                                updateEntry.handleWriteUpdateInsulin(logger.getChatId(), logger.getMessage(), userStMap, message);

                        case WAIT_NOTE_FOR_UPDATE ->
                                updateEntry.handleWriteUpdateNote(logger.getChatId(), logger.getMessage(), userStMap, message);

                        case WAIT_TIME_FOR_NOTIFY ->
                                notification.setNotify(logger.getChatId(), logger.getMessage(), message, menu, userStMap);

                        case WAITING_FOR_DATES ->
                                getForPeriod.returnEntryList(logger.getChatId(), logger.getMessage(), message, userStMap);
                    }

                } else if (logger.getMessage().equals("/start")) {
                    menu.sendMenu(logger.getChatId(), message);

                } else if (logger.getMessage().equals("/help")) {
                    message.execute(message.sendMessage(logger.getChatId(), "@".concat(System.getenv("ADMIN"))));

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
                case "addEntry" -> addEntry.start(chatId, message, userStMap);

                case "updateEntry" -> updateEntry.updateStart(chatId, userStMap, message);

                case "getById" -> getById.getBySugarIdStart(chatId, userStMap, message);

                case "removeById" -> delete.removeStart(chatId, userStMap, message);

                case "createNotify" -> notification.sendNotify(chatId, message, userStMap);

                case "disableNotify" -> notification.disableNotify(chatId, message, userStMap);

                case "getEntryForPeriod" -> getForPeriod.requestPeriod(chatId, message, userStMap);

                case "checkMyNotify" -> notification.checkMyNotify(chatId, message);

                default -> message.execute(message.sendMessage(chatId, "Не известная команда 🤷‍♂️"));
            }
        }
    }
}
