package sugar.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import sugar.sugar.service.SugarServiceImpl;
import sugar.telegram.addEntry.AddEntry;
import sugar.telegram.clear.Delete;
import sugar.telegram.get.Get;
import sugar.telegram.loger.Logger;
import sugar.telegram.menu.Menu;
import sugar.telegram.notification.Notification;
import sugar.telegram.state.UserSt;
import sugar.telegram.update.UpdateEntry;
import sugar.telegram.util.file.FileWriter;
import sugar.telegram.util.message.Message;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@RequiredArgsConstructor
@Slf4j
@Component
public class CommandFilter implements LongPollingSingleThreadUpdateConsumer {

    private final Message message;
    private final FileWriter writer;
    private final AddEntry addEntry;
    private final Delete delete;
    private final Get get;
    private final UpdateEntry updateEntry;
    private final Notification notification;
    private final SugarServiceImpl sugarService;
    private static final File file = new File("logger.txt");
    private Set<Long> setChatId = new HashSet<>();
    private final Map<Long, UserSt> userStMap = new TreeMap<>();

    @Override
    public void consume(Update update) {

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
                        case SUGAR ->
                                addEntry.handleWriteSugar(logger.getChatId(), logger.getMessage(), userStMap, message);

                        case INSULIN ->
                                addEntry.handleWriteInsulin(logger.getChatId(), logger.getMessage(), userStMap, message);

                        case NOTE ->
                                addEntry.handleWriteNote(logger.getChatId(), logger.getMessage(), userStMap, message, sugarService);

                        case REMOVE ->
                                delete.removeById(logger.getChatId(), logger.getMessage(), sugarService, message, userStMap);

                        case GET_SUGAR_BY_ID ->
                                get.getSugarById(logger.getChatId(), logger.getMessage(), sugarService, message, userStMap);

                        case WAIT_ID_FOR_UPDATE ->
                                updateEntry.sugarUpdate(logger.getChatId(), logger.getMessage(), userSt, sugarService, message, userStMap);

                        case UPDATE_START ->
                                updateEntry.begin(logger.getChatId(), logger.getMessage(), userStMap, message, sugarService);

                        case WAIT_SUGAR_FOR_UPDATE ->
                                updateEntry.handleWriteUpdateSugar(logger.getChatId(), logger.getMessage(), userStMap, message);

                        case WAIT_INSULIN_FOR_UPDATE ->
                                updateEntry.handleWriteUpdateInsulin(logger.getChatId(), logger.getMessage(), userStMap, message);

                        case WAIT_NOTE_FOR_UPDATE ->
                                updateEntry.handleWriteUpdateNote(logger.getChatId(), logger.getMessage(), userStMap, message);
                    }

                } else if (logger.getMessage().equals("/start")) {
                    Menu.sendMenu(logger.getChatId(), message);

                } else if (logger.getMessage().equals("/help")) {
                    message.execute(message.sendMessage(logger.getChatId(), "@".concat(System.getenv("admin"))));

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

                case "getById" -> get.getBySugarIdStart(chatId, userStMap, message);

                case "removeById" -> delete.removeStart(chatId, userStMap, message);

                default -> message.execute(message.sendMessage(chatId, "Не известная команда 🤷‍♂️"));
            }
        }
    }
}
