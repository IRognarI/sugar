package sugar.telegram.update;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import sugar.sugar.dto.NewSugar;
import sugar.sugar.dto.SugarDto;
import sugar.sugar.dto.UpdateSugar;
import sugar.sugar.exception.NotFoundException;
import sugar.sugar.exception.ValidationException;
import sugar.sugar.service.SugarServiceImpl;
import sugar.sugar.util.dateTimeFormater.DateTimeFormat;
import sugar.telegram.enums.State;
import sugar.telegram.loger.Logger;
import sugar.telegram.state.UserSt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final SugarServiceImpl sugarService;
    private final Map<Long, UserSt> userStMap = new TreeMap<>();
    private final List<Long> targetChatId = List.of(Long.parseLong(System.getenv("my_chat_token")));
    private static final String admin = "t_visitor";
    private static final File file = new File("logger.txt");


    @Override
    @SneakyThrows
    public void consume(Update update) {

        if (update.hasMessage()) {

            if (update.getMessage().hasText()) {
                Logger logger = new Logger(update.getMessage().getChatId(), update.getMessage().getText());
                fileWriter(logger, file);
                log.info("Новое сообщение: {}", logger);

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
                    execute(message(logger.getChatId(), "Скоро здесь появится инструкция"));

                } else {
                    execute(message(logger.getChatId(), "click 👉 /start"));
                }

            } else {
                execute(message(update.getMessage().getChatId(), "Я еще в мастерской 🥲"));
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

                default -> execute(message(chatId, "Не известная команда 🤷‍♂️"));
            }
        }
    }

    private void fileWriter(Logger logger, File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        boolean append = true;

        long fileSize = file.length();
        long maxSize = 262144000L;

        if (fileSize > maxSize) {
            try {
                Files.write(file.toPath(), new byte[0]);
                log.info("Файл logger.txt - очищен");
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        try (BufferedWriter writer = new BufferedWriter((new FileWriter(file, append)))) {

            writer.write("{ChatId=" + logger.getChatId() + ";\n" + "Message=" + logger.getMessage() + ";\n" + "Time=" + DateTimeFormat.dateTimeToString(logger.getDateTime()) + "};\n\n");

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void handleWriteUpdateNote(Long chatId, String message) {
        UserSt userSt = userStMap.get(chatId);

        if (userSt == null) {
            execute(message(chatId, "Ошибка обновления. Свяжитесь с @" + admin));
            return;
        }

        UpdateSugar updateSugar = userSt.getUpdate();

        if (message != null && !message.isEmpty()) {

            updateSugar.setNote(message.trim());
            execute(message(chatId, "Заметка добавлена"));
        }

        userSt.setState(State.UPDATE_START);
        userStMap.put(chatId, userSt);
    }

    private void handleWriteUpdateInsulin(Long chatId, String message) {
        if (message != null && !message.isEmpty()) {

            UserSt userSt = userStMap.get(chatId);
            UpdateSugar updateSugar = userSt.getUpdate();

            try {
                updateSugar.setDoseOfInsulin(Double.parseDouble(message.trim()));
                execute(message(chatId, "Инсулин добавлен"));
                userSt.setState(State.UPDATE_START);
                userStMap.put(chatId, userSt);

            } catch (NumberFormatException e) {
                execute(message(chatId, "Инсулин указывается через точку. Пример: 1.5"));
            }
        } else {
            execute(message(chatId, "Укажите инсулин. Пример: 1.5"));
        }
    }

    private void handleWriteUpdateSugar(Long chatId, String message) {
        if (message != null && !message.isEmpty()) {

            UserSt userSt = userStMap.get(chatId);
            UpdateSugar updateSugar = userSt.getUpdate();

            try {
                updateSugar.setSugarLevel(Double.parseDouble(message.trim()));
                execute(message(chatId, "Сахар добавлен"));
                userSt.setState(State.UPDATE_START);
                userStMap.put(chatId, userSt);

            } catch (NumberFormatException e) {
                execute(message(chatId, "Сахар указывается через точку. Пример: 7.1"));
            }
        } else {
            execute(message(chatId, "Укажите сахар. Пример: 7.1"));
        }
    }

    private void begin(Long chatId, String message) {
        if (message != null && !message.isEmpty()) {
            log.debug("Получили сообщение в begin: {}", message);

            UserSt userSt = userStMap.get(chatId);

            if (userSt == null) {
                execute(message(chatId, "Ошибка обновления. Свяжитесь с @" + admin));
                return;
            }

            UpdateSugar updateSugar = userSt.getUpdate();

            switch (message) {
                case "/end":
                    SugarDto sugarDto = sugarService.updateEntry(updateSugar);
                    execute(message(chatId, String.format("Обновленная запись:%n%n%s", answerAfterSaved(sugarDto))));
                    userStMap.remove(chatId);
                    sendMenu(chatId);
                    break;

                case "/sugar":
                    execute(message(chatId, "Укажите сахар"));
                    userSt.setState(State.WAIT_SUGAR_FOR_UPDATE);
                    userStMap.put(chatId, userSt);
                    break;

                case "/insulin":
                    execute(message(chatId, "Укажите инсулин"));
                    userSt.setState(State.WAIT_INSULIN_FOR_UPDATE);
                    userStMap.put(chatId, userSt);
                    break;

                case "/note":
                    execute(message(chatId, "Укажите заметку"));
                    userSt.setState(State.WAIT_NOTE_FOR_UPDATE);
                    userStMap.put(chatId, userSt);
                    break;

                default:
                    execute(message(chatId, "Не известная команда"));
            }
        } else {
            log.info("{} не отправил команду для обновления", chatId);
            execute(message(chatId, "Отправьте команду для обновления"));
        }
    }

    private void sugarUpdate(Long chatId, String message, UserSt userSt) {
        if (message != null && !message.isEmpty()) {

            try {
                SugarDto sugarDto = sugarService.getSugarById(Long.parseLong(message.trim()));
                log.info("Получили ID= {} для обновления записи", sugarDto.getSugarId());

                UpdateSugar updateSugar = new UpdateSugar();

                userSt.setUpdate(updateSugar);
                userSt.getUpdate().setSugarId(sugarDto.getSugarId());

                execute(message(chatId, "Нажмите на \"Меню\" и отправьте данные для обновления. После обновления отправьте \"end\""));
                userSt.setState(State.UPDATE_START);
                userStMap.put(chatId, userSt);

            } catch (ValidationException | NotFoundException e) {
                log.debug("Исключение в sugarUpdate. {}", e.getMessage());
                execute(message(chatId, e.getMessage()));
            }
        }
    }

    private void updateStart(Long chatId) {
        UserSt userSt = userStMap.get(chatId);

        if (userSt == null) {
            userSt = new UserSt();
        }

        userSt.setState(State.WAIT_ID_FOR_UPDATE);
        userStMap.put(chatId, userSt);

        log.info("{} начинает обновление записи", chatId);
        execute(message(chatId, "Отправьте ID записи для обновления"));
    }

    private void getSugarById(Long chatId, String message) {
        if (message != null && !message.isEmpty()) {

            try {
                Long sugarId = Long.parseLong(message.trim());
                log.info("Пользователь {} хочет получить запись с ID {}", chatId, sugarId);

                try {
                    SugarDto sugarDto = sugarService.getSugarById(sugarId);

                    execute(message(chatId, answerAfterSaved(sugarDto)));
                    userStMap.remove(chatId);
                    sendMenu(chatId);
                } catch (ValidationException | NotFoundException e) {
                    execute(message(chatId, e.getMessage()));
                }

            } catch (NumberFormatException e) {
                log.debug("{} отправил не корректный ID записи", chatId);
                execute(message(chatId, "Отправьте просто число. Например: 7"));
            }
        } else {
            log.debug("{} отправил не корректное сообщение: {}", chatId, message);
            execute(message(chatId, "Отправьте ID записи, например: 7"));
        }
    }

    private void getBySugarIdStart(Long chatId) {
        UserSt userSt = userStMap.get(chatId);

        if (userSt == null) {
            userSt = new UserSt();
        }
        userSt.setState(State.GET_SUGAR_BY_ID);
        userStMap.put(chatId, userSt);
        execute(message(chatId, "Отправьте ID записи"));
    }

    private void removeById(Long chatId, String message) {
        if (message != null && !message.isEmpty()) {
            try {
                Long sugarId = Long.parseLong(message.trim());
                log.info("Передан ID для удаления: {}", sugarId);

                SugarDto entryExists = null;
                try {
                    entryExists = sugarService.getSugarById(sugarId);

                } catch (NotFoundException e) {
                    execute(message(chatId, e.getMessage()));
                }


                if (entryExists != null) {
                    sugarService.removeSugarById(sugarId);
                    userStMap.remove(chatId);
                    execute(message(chatId, "Запись с ID= " + sugarId + " - была удалена"));
                    sendMenu(chatId);
                }

            } catch (NumberFormatException e) {
                log.debug("Указали не верный формат ID в removeById");
                execute(message(chatId, "Укажите просто число. Пример: 7"));
            }
        }
    }

    private void removeStart(Long chatId) {
        UserSt userSt = userStMap.get(chatId);

        boolean thisIsAdmin = targetChatId.contains(chatId);
        log.info("Для удаления записи обратился admin: {}", thisIsAdmin);

        if (thisIsAdmin) {

            if (userSt == null) {
                userSt = new UserSt();
            }

            userSt.setState(State.REMOVE);
            userStMap.put(chatId, userSt);

            execute(message(chatId, "Отправьте ID записи, которую желаете удалить"));

        } else {
            log.info("Пользователь {} хочет удалить запись", chatId);
            execute(message(chatId, "В доступе отказано. Обратитесь к @" + admin));
        }
    }

    private String answerAfterSaved(SugarDto sugarDto) {

        return format("Предыдущая запись:%n%nID прошлой записи: %d%nДоза инсулина при сахаре: %.1f - %.2f%n" + "Последний раз, когда сахар был: %.1f - %s%n%nТекущая запись:%n%nID: %d%nСахар: %.1f%nИнсулин: %.2f%nВремя: %s%nЗаметка: %s", sugarDto.getId(), sugarDto.getLevelSugar(), sugarDto.getLastDoseOfInsulin(), sugarDto.getLevelSugar(), sugarDto.getLastDate(), sugarDto.getSugarId(), sugarDto.getLevelSugar(), sugarDto.getDoseOfInsulin(), sugarDto.getTime(), sugarDto.getNote());
    }

    private void handleWriteNote(Long chatId, String message) {
        UserSt userSt = userStMap.get(chatId);

        if (message != null && !message.isEmpty() && !message.equals(".")) {

            userSt.getNewSugar().setNote(message.trim());
            log.debug("Новая заметка {}", userSt.getNewSugar().getNote());
        } else {
            execute(message(chatId, "Заметка остается по умолчанию: " + userSt.getNewSugar().getNote()));
        }

        SugarDto sugarDto = sugarService.addEntry(userStMap.get(chatId).getNewSugar());
        execute(message(chatId, answerAfterSaved(sugarDto)));

        userStMap.remove(chatId);
        sendMenu(chatId);
    }

    private void handleWriteInsulin(Long chatId, String message) {
        UserSt userSt = userStMap.get(chatId);

        if (message != null && !message.isEmpty() && !message.equals(".")) {


            try {
                double insulin = Double.parseDouble(message.trim());
                userSt.getNewSugar().setDoseOfInsulin(insulin);
                log.debug("Записали инсулин в handleWriteInsulin: {}", userSt.getNewSugar().getDoseOfInsulin());

                execute(message(chatId, "Можно добавить заметку 📝. Если не нужна введите точку"));
                userSt.setState(State.NOTE);

                userStMap.put(chatId, userSt);
            } catch (NumberFormatException e) {
                log.debug("Выброшен NumberFormatException в handleWriteInsulin");
                execute(message(chatId, "Инсулин указывается через точку. Пример: 1.5"));
            }

        } else {
            execute(message(chatId, "Инсулин остается: " + userSt.getNewSugar().getDoseOfInsulin()));

            execute(message(chatId, "Можно добавить заметку 📝. Если не нужна введите точку"));
            userSt.setState(State.NOTE);

            userStMap.put(chatId, userSt);
        }
    }

    private void handleWriteSugar(Long chatId, String message) {
        UserSt userSt = userStMap.get(chatId);

        if (message != null && !message.isEmpty()) {

            try {
                double sugarLevel = Double.parseDouble(message.trim());
                userSt.getNewSugar().setSugarLevel(sugarLevel);
                log.debug("Записали сахар в handleWriteSugar: {}", userSt.getNewSugar().getSugarLevel());

                execute(message(chatId, "Укажите дозу инсулина 👇 Если указывать не нужно, отправьте точку"));
                userSt.setState(State.INSULIN);

                userStMap.put(chatId, userSt);
            } catch (NumberFormatException e) {
                log.debug("Выброшено исключение NumberFormatException в handleWriteSugar");
                execute(message(chatId, "Уровень сахара указывается через точку. Пример: 9.2"));
            }

        } else {
            log.debug("Не указали уровень сахара");
            execute(message(chatId, "Уровень сахара должен быть указан"));
        }
    }

    private void start(Long chatId) {
        UserSt userSt = new UserSt();
        NewSugar newSugar = new NewSugar();
        userSt.setNewSugar(newSugar);

        execute(message(chatId, "Введите сахар"));

        userSt.setState(State.SUGAR);

        userStMap.put(chatId, userSt);
    }

    private void execute(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Сообщение не было отправлено: {}", e.getMessage());
        }
    }

    private SendMessage message(Long chatId, String message) {
        return SendMessage.builder().chatId(chatId).text(message).build();
    }

    private void sendMenu(Long chatId) {
        SendMessage sendMessage = message(chatId, "Вот что я могу:");

        InlineKeyboardButton button1 = InlineKeyboardButton.builder().text("Создать запись").callbackData("addEntry").build();

        InlineKeyboardButton button2 = InlineKeyboardButton.builder().text("Обновить запись").callbackData("updateEntry").build();

        InlineKeyboardButton button3 = InlineKeyboardButton.builder().text("Получить запись по ID").callbackData("getById").build();

        InlineKeyboardButton button4 = InlineKeyboardButton.builder().text("Удалить запись").callbackData("removeById").build();

        List<InlineKeyboardRow> keyboards = List.of(new InlineKeyboardRow(button1), new InlineKeyboardRow(button2), new InlineKeyboardRow(button3), new InlineKeyboardRow(button4));

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(keyboards);

        sendMessage.setReplyMarkup(keyboardMarkup);

        execute(sendMessage);
    }
}
