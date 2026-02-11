package sugar_bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import sugar_bot.sugar.interfaces.SugarService;
import sugar_bot.telegram.addEntry.AddEntry;
import sugar_bot.telegram.atFirst.AtFirst;
import sugar_bot.telegram.clear.Delete;
import sugar_bot.telegram.get.getById.GetById;
import sugar_bot.telegram.get.getForPeriod.GetForPeriod;
import sugar_bot.telegram.loger.Logger;
import sugar_bot.telegram.menu.Menu;
import sugar_bot.telegram.notification.Notification;
import sugar_bot.telegram.update.UpdateEntry;
import sugar_bot.telegram.userCheck.UserCheck;
import sugar_bot.telegram.util.message.Message;

import java.util.Map;
import java.util.TreeMap;

@RequiredArgsConstructor
@Slf4j
@Component
public class CommandFilter implements LongPollingSingleThreadUpdateConsumer {

    private final Menu menu;
    private final Message message;
    private final AddEntry addEntry;
    private final Delete delete;
    private final GetById getById;
    private final UpdateEntry updateEntry;
    private final Notification notification;
    private final SugarService sugarService;
    private final GetForPeriod getForPeriod;
    private final AtFirst atFirst;
    private final Map<Long, UserCheck> userCheckMap = new TreeMap<>();

    @Override
    public void consume(Update update) {

        if (update.hasMessage()) {

            if (update.getMessage().hasText()) {

                Logger logger = new Logger(update.getMessage().getChatId(), update.getMessage().getText());
                log.info("Новое сообщение: {}", logger);

                UserCheck userCheck = userCheckMap.get(logger.getChatId());

                if (userCheck != null) {

                    switch (userCheck.getState()) {

                        case START -> menu.sendMenu(logger.getChatId(), message);

                        case SUGAR ->
                                addEntry.handleWriteSugar(logger.getChatId(), logger.getMessage(), userCheckMap, message);

                        case INSULIN ->
                                addEntry.handleWriteInsulin(logger.getChatId(), logger.getMessage(), userCheckMap, message);

                        case NOTE ->
                                addEntry.handleWriteNote(logger.getChatId(), logger.getMessage(), userCheckMap, message, menu, sugarService);

                        case REMOVE ->
                                delete.removeById(logger.getChatId(), logger.getMessage(), sugarService, message, menu, userCheckMap);

                        case GET_SUGAR_BY_ID ->
                                getById.getSugarById(logger.getChatId(), logger.getMessage(), sugarService, message, menu, userCheckMap);

                        case WAIT_ID_FOR_UPDATE ->
                                updateEntry.sugarUpdate(logger.getChatId(), logger.getMessage(), userCheck, sugarService, message, userCheckMap);

                        case UPDATE_START ->
                                updateEntry.begin(logger.getChatId(), logger.getMessage(), userCheckMap, message, menu, sugarService);

                        case WAIT_SUGAR_FOR_UPDATE ->
                                updateEntry.handleWriteUpdateSugar(logger.getChatId(), logger.getMessage(), userCheckMap, message);

                        case WAIT_INSULIN_FOR_UPDATE ->
                                updateEntry.handleWriteUpdateInsulin(logger.getChatId(), logger.getMessage(), userCheckMap, message);

                        case WAIT_NOTE_FOR_UPDATE ->
                                updateEntry.handleWriteUpdateNote(logger.getChatId(), logger.getMessage(), userCheckMap, message);

                        case WAIT_TIME_FOR_NOTIFY ->
                                notification.setNotify(logger.getChatId(), logger.getMessage(), message, menu, userCheckMap);

                        case WAITING_FOR_DATES ->
                                getForPeriod.returnEntryList(logger.getChatId(), logger.getMessage(), message, userCheckMap);
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
                case "addEntry" -> addEntry.start(chatId, message, userCheckMap);

                case "updateEntry" -> updateEntry.updateStart(chatId, userCheckMap, message);

                case "getById" -> getById.getBySugarIdStart(chatId, userCheckMap, message);

                case "removeById" -> delete.removeStart(chatId, userCheckMap, message);

                case "createNotify" -> notification.sendNotify(chatId, message, userCheckMap);

                case "disableNotify" -> notification.disableNotify(chatId, message, userCheckMap);

                case "getEntryForPeriod" -> getForPeriod.requestPeriod(chatId, message, userCheckMap);

                case "checkMyNotify" -> notification.checkMyNotify(chatId, message);

                case "atFirst" -> atFirst.atFirst(chatId, message, userCheckMap);

                default -> message.execute(message.sendMessage(chatId, "Не известная команда 🤷‍♂️"));
            }
        }
    }
}
