package sugar_bot.telegram.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar_bot.sugar.dto.SugarDto;
import sugar_bot.sugar.dto.UpdateSugar;
import sugar_bot.sugar.exception.NotFoundException;
import sugar_bot.sugar.exception.ValidationException;
import sugar_bot.sugar.interfaces.SugarService;
import sugar_bot.telegram.enums.State;
import sugar_bot.telegram.menu.Menu;
import sugar_bot.telegram.userCheck.UserCheck;
import sugar_bot.telegram.util.admin.Admin;
import sugar_bot.telegram.util.message.Message;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateEntry {
    public void updateStart(Long chatId, Map<Long, UserCheck> userStMap, Message message) {
        UserCheck userCheck = userStMap.get(chatId);

        if (userCheck == null) {
            userCheck = new UserCheck();
        }

        userCheck.setState(State.WAIT_ID_FOR_UPDATE);
        userStMap.put(chatId, userCheck);

        log.info("{} начинает обновление записи", chatId);
        message.execute(message.sendMessage(chatId, "Отправьте ID записи для обновления"));
    }

    public void begin(Long chatId, String note, Map<Long, UserCheck> userStMap, Message message, Menu menu, SugarService sugarService) {
        if (note != null && !note.isEmpty()) {

            log.debug("Получили сообщение в begin: {}", note);

            UserCheck userCheck = userStMap.get(chatId);

            if (userCheck == null) {
                message.execute(message.sendMessage(chatId, "Ошибка обновления. Свяжитесь с @" + Admin.getAdmin()));
                return;
            }

            UpdateSugar updateSugar = userCheck.getUpdate();
            updateSugar.setChatId(chatId);

            switch (note) {
                case "/end":
                    SugarDto sugarDto = sugarService.updateEntry(updateSugar);
                    message.execute(message.sendMessage(chatId, String.format("Обновленная запись:%n%n%s", message.answerAfterSaved(sugarDto))));
                    userStMap.get(chatId).setState(State.START);
                    menu.sendMenu(chatId, message);
                    break;

                case "/sugar":
                    message.execute(message.sendMessage(chatId, "Укажите сахар"));
                    userCheck.setState(State.WAIT_SUGAR_FOR_UPDATE);
                    userStMap.put(chatId, userCheck);
                    break;

                case "/insulin":
                    message.execute(message.sendMessage(chatId, "Укажите инсулин"));
                    userCheck.setState(State.WAIT_INSULIN_FOR_UPDATE);
                    userStMap.put(chatId, userCheck);
                    break;

                case "/note":
                    message.execute(message.sendMessage(chatId, "Укажите заметку"));
                    userCheck.setState(State.WAIT_NOTE_FOR_UPDATE);
                    userStMap.put(chatId, userCheck);
                    break;

                default:
                    message.execute(message.sendMessage(chatId, "Не известная команда. Начните сначала"));
                    userCheck.setState(State.START);
                    userStMap.put(chatId, userCheck);
            }
        } else {
            log.info("{} не отправил команду для обновления", chatId);
            message.execute(message.sendMessage(chatId, "Отправьте команду для обновления"));
        }
    }

    public void sugarUpdate(Long chatId, String note, UserCheck userCheck, SugarService sugarService, Message message, Map<Long, UserCheck> userStMap) {
        if (note != null && !note.isEmpty()) {

            try {
                SugarDto sugarDto = sugarService.getSugarById(Long.parseLong(note.trim()), chatId);
                log.info("Получили ID= {} для обновления записи", sugarDto.getSugarId());

                UpdateSugar updateSugar = new UpdateSugar();

                userCheck.setUpdate(updateSugar);
                userCheck.getUpdate().setSugarId(sugarDto.getSugarId());

                message.execute(message.sendMessage(chatId, "Нажмите на \"Меню\" и отправьте данные для обновления. После обновления отправьте /end\n\n" +
                        "Для обновления, используйте команды:\n/sugar - обновить сахар\n/insulin - обновить дозу инсулина\n/note - обновить заметку"));
                userCheck.setState(State.UPDATE_START);
                userStMap.put(chatId, userCheck);

            } catch (ValidationException | NotFoundException e) {
                log.debug("Исключение в sugarUpdate. {}", e.getMessage());
                message.execute(message.sendMessage(chatId, e.getMessage()));

            } catch (NumberFormatException e) {
                message.execute(message.sendMessage(chatId, "Укажите просто число, например: 7"));
            }
        }
    }

    public void handleWriteUpdateSugar(Long chatId, String note, Map<Long, UserCheck> userStMap, Message message) {
        if (note != null && !note.isEmpty()) {

            UserCheck userCheck = userStMap.get(chatId);
            UpdateSugar updateSugar = userCheck.getUpdate();

            try {
                updateSugar.setSugarLevel(Double.parseDouble(note.trim()));
                message.execute(message.sendMessage(chatId, "Сахар добавлен"));
                userCheck.setState(State.UPDATE_START);
                userStMap.put(chatId, userCheck);

            } catch (NumberFormatException e) {
                message.execute(message.sendMessage(chatId, "Сахар указывается через точку. Пример: 7.1"));
            }
        } else {
            message.execute(message.sendMessage(chatId, "Укажите сахар. Пример: 7.1"));
        }
    }

    public void handleWriteUpdateInsulin(Long chatId, String note, Map<Long, UserCheck> userStMap, Message message) {
        if (note != null && !note.isEmpty()) {

            UserCheck userCheck = userStMap.get(chatId);
            UpdateSugar updateSugar = userCheck.getUpdate();

            try {
                updateSugar.setDoseOfInsulin(Double.parseDouble(note.trim()));
                message.execute(message.sendMessage(chatId, "Инсулин добавлен"));
                userCheck.setState(State.UPDATE_START);
                userStMap.put(chatId, userCheck);

            } catch (NumberFormatException e) {
                message.execute(message.sendMessage(chatId, "Инсулин указывается через точку. Пример: 1.5"));
            }
        } else {
            message.execute(message.sendMessage(chatId, "Укажите инсулин. Пример: 1.5"));
        }
    }

    public void handleWriteUpdateNote(Long chatId, String note, Map<Long, UserCheck> userStMap, Message message) {
        UserCheck userCheck = userStMap.get(chatId);

        if (userCheck == null) {
            message.execute(message.sendMessage(chatId, "Ошибка обновления. Свяжитесь с @" + Admin.getAdmin()));
            return;
        }

        UpdateSugar updateSugar = userCheck.getUpdate();

        if (note != null && !note.isEmpty()) {

            updateSugar.setNote(note.trim());
            message.execute(message.sendMessage(chatId, "Заметка добавлена"));
        }

        userCheck.setState(State.UPDATE_START);
        userStMap.put(chatId, userCheck);
    }
}
