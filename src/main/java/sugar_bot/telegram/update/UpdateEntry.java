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
        message.sendMessage(chatId, "Отправьте ID записи для обновления", null);
    }

    public void begin(Long chatId, String note, Map<Long, UserCheck> userStMap, Message message, Menu menu, SugarService sugarService) {
        if (note != null && !note.isEmpty()) {

            log.debug("Получили сообщение в begin: {}", note);

            UserCheck userCheck = userStMap.get(chatId);

            if (userCheck == null) {
                message.sendMessage(chatId, "Ошибка обновления. Свяжитесь с @" + Admin.getAdmin(), null);
                return;
            }

            UpdateSugar updateSugar = userCheck.getUpdate();
            updateSugar.setChatId(chatId);

            switch (note) {
                case "/end":
                    SugarDto sugarDto = sugarService.updateEntry(updateSugar);
                    message.sendMessage(chatId, String.format("Обновленная запись:%n%n%s", message.answerAfterSaved(sugarDto)), null);
                    userStMap.get(chatId).setState(State.START);
                    menu.sendMenu(chatId, message);
                    break;

                case "/sugar":
                    message.sendMessage(chatId, "Укажите сахар", null);
                    userCheck.setState(State.WAIT_SUGAR_FOR_UPDATE);
                    userStMap.put(chatId, userCheck);
                    break;

                case "/insulin":
                    message.sendMessage(chatId, "Укажите инсулин", null);
                    userCheck.setState(State.WAIT_INSULIN_FOR_UPDATE);
                    userStMap.put(chatId, userCheck);
                    break;

                case "/note":
                    message.sendMessage(chatId, "Укажите заметку", null);
                    userCheck.setState(State.WAIT_NOTE_FOR_UPDATE);
                    userStMap.put(chatId, userCheck);
                    break;

                default:
                    message.sendMessage(chatId, "Не известная команда. Начните сначала", null);
                    userCheck.setState(State.START);
                    userStMap.put(chatId, userCheck);
            }
        } else {
            log.info("{} не отправил команду для обновления", chatId);
            message.sendMessage(chatId, "Отправьте команду для обновления", null);
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

                message.sendMessage(chatId, "Для обновления, используйте команды:\n" +
                        "/sugar - обновить сахар\n" +
                        "/insulin - обновить дозу инсулина\n" +
                        "/note - обновить заметку\n" +
                        "\nПо завершению обновления отправьте:\n/end", null);

                userCheck.setState(State.UPDATE_START);
                userStMap.put(chatId, userCheck);

            } catch (ValidationException | NotFoundException e) {
                log.debug("Исключение в sugarUpdate. {}", e.getMessage());
                message.sendMessage(chatId, e.getMessage(), null);

            } catch (NumberFormatException e) {
                message.sendMessage(chatId, "Укажите просто число, например: 7", null);
            }
        }
    }

    public void handleWriteUpdateSugar(Long chatId, String note, Map<Long, UserCheck> userStMap, Message message) {
        if (note != null && !note.isEmpty()) {

            UserCheck userCheck = userStMap.get(chatId);
            UpdateSugar updateSugar = userCheck.getUpdate();

            try {
                updateSugar.setSugarLevel(Double.parseDouble(note.trim()));
                message.sendMessage(chatId, "Сахар добавлен", null);
                userCheck.setState(State.UPDATE_START);
                userStMap.put(chatId, userCheck);

            } catch (NumberFormatException e) {
                message.sendMessage(chatId, "Сахар указывается через точку. Пример: 7.1", null);
            }
        } else {
            message.sendMessage(chatId, "Укажите сахар. Пример: 7.1", null);
        }
    }

    public void handleWriteUpdateInsulin(Long chatId, String note, Map<Long, UserCheck> userStMap, Message message) {
        if (note != null && !note.isEmpty()) {

            UserCheck userCheck = userStMap.get(chatId);
            UpdateSugar updateSugar = userCheck.getUpdate();

            try {
                updateSugar.setDoseOfInsulin(Double.parseDouble(note.trim()));
                message.sendMessage(chatId, "Инсулин добавлен", null);
                userCheck.setState(State.UPDATE_START);
                userStMap.put(chatId, userCheck);

            } catch (NumberFormatException e) {
                message.sendMessage(chatId, "Инсулин указывается через точку. Пример: 1.5", null);
            }
        } else {
            message.sendMessage(chatId, "Укажите инсулин. Пример: 1.5", null);
        }
    }

    public void handleWriteUpdateNote(Long chatId, String note, Map<Long, UserCheck> userStMap, Message message) {
        UserCheck userCheck = userStMap.get(chatId);

        if (userCheck == null) {
            message.sendMessage(chatId, "Ошибка обновления. Свяжитесь с @" + Admin.getAdmin(), null);
            return;
        }

        UpdateSugar updateSugar = userCheck.getUpdate();

        if (note != null && !note.isEmpty()) {

            updateSugar.setNote(note.trim());
            message.sendMessage(chatId, "Заметка добавлена", null);
        }

        userCheck.setState(State.UPDATE_START);
        userStMap.put(chatId, userCheck);
    }
}
