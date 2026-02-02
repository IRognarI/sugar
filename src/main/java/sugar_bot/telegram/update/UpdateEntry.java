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
import sugar_bot.telegram.state.UserSt;
import sugar_bot.telegram.util.admin.Admin;
import sugar_bot.telegram.util.message.Message;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateEntry {
    public void updateStart(Long chatId, Map<Long, UserSt> userStMap, Message message) {
        UserSt userSt = userStMap.get(chatId);

        if (userSt == null) {
            userSt = new UserSt();
        }

        userSt.setState(State.WAIT_ID_FOR_UPDATE);
        userStMap.put(chatId, userSt);

        log.info("{} начинает обновление записи", chatId);
        message.execute(message.sendMessage(chatId, "Отправьте ID записи для обновления"));
    }

    public void begin(Long chatId, String note, Map<Long, UserSt> userStMap, Message message, SugarService sugarService) {
        if (note != null && !note.isEmpty()) {
            log.debug("Получили сообщение в begin: {}", note);

            UserSt userSt = userStMap.get(chatId);

            if (userSt == null) {
                message.execute(message.sendMessage(chatId, "Ошибка обновления. Свяжитесь с @" + Admin.getAdmin()));
                return;
            }

            UpdateSugar updateSugar = userSt.getUpdate();

            switch (note) {
                case "/end":
                    SugarDto sugarDto = sugarService.updateEntry(updateSugar);
                    message.execute(message.sendMessage(chatId, String.format("Обновленная запись:%n%n%s", message.answerAfterSaved(sugarDto))));
                    userStMap.get(chatId).setState(State.START);
                    Menu.sendMenu(chatId, message);
                    break;

                case "/sugar":
                    message.execute(message.sendMessage(chatId, "Укажите сахар"));
                    userSt.setState(State.WAIT_SUGAR_FOR_UPDATE);
                    userStMap.put(chatId, userSt);
                    break;

                case "/insulin":
                    message.execute(message.sendMessage(chatId, "Укажите инсулин"));
                    userSt.setState(State.WAIT_INSULIN_FOR_UPDATE);
                    userStMap.put(chatId, userSt);
                    break;

                case "/note":
                    message.execute(message.sendMessage(chatId, "Укажите заметку"));
                    userSt.setState(State.WAIT_NOTE_FOR_UPDATE);
                    userStMap.put(chatId, userSt);
                    break;

                default:
                    message.execute(message.sendMessage(chatId, "Не известная команда"));
            }
        } else {
            log.info("{} не отправил команду для обновления", chatId);
            message.execute(message.sendMessage(chatId, "Отправьте команду для обновления"));
        }
    }

    public void sugarUpdate(Long chatId, String note, UserSt userSt, SugarService sugarService, Message message, Map<Long, UserSt> userStMap) {
        if (note != null && !note.isEmpty()) {

            try {
                SugarDto sugarDto = sugarService.getSugarById(Long.parseLong(note.trim()));
                log.info("Получили ID= {} для обновления записи", sugarDto.getSugarId());

                UpdateSugar updateSugar = new UpdateSugar();

                userSt.setUpdate(updateSugar);
                userSt.getUpdate().setSugarId(sugarDto.getSugarId());

                message.execute(message.sendMessage(chatId, "Нажмите на \"Меню\" и отправьте данные для обновления. После обновления отправьте \"end\""));
                userSt.setState(State.UPDATE_START);
                userStMap.put(chatId, userSt);

            } catch (ValidationException | NotFoundException e) {
                log.debug("Исключение в sugarUpdate. {}", e.getMessage());
                message.execute(message.sendMessage(chatId, e.getMessage()));
            }
        }
    }

    public void handleWriteUpdateSugar(Long chatId, String note, Map<Long, UserSt> userStMap, Message message) {
        if (note != null && !note.isEmpty()) {

            UserSt userSt = userStMap.get(chatId);
            UpdateSugar updateSugar = userSt.getUpdate();

            try {
                updateSugar.setSugarLevel(Double.parseDouble(note.trim()));
                message.execute(message.sendMessage(chatId, "Сахар добавлен"));
                userSt.setState(State.UPDATE_START);
                userStMap.put(chatId, userSt);

            } catch (NumberFormatException e) {
                message.execute(message.sendMessage(chatId, "Сахар указывается через точку. Пример: 7.1"));
            }
        } else {
            message.execute(message.sendMessage(chatId, "Укажите сахар. Пример: 7.1"));
        }
    }

    public void handleWriteUpdateInsulin(Long chatId, String note, Map<Long, UserSt> userStMap, Message message) {
        if (note != null && !note.isEmpty()) {

            UserSt userSt = userStMap.get(chatId);
            UpdateSugar updateSugar = userSt.getUpdate();

            try {
                updateSugar.setDoseOfInsulin(Double.parseDouble(note.trim()));
                message.execute(message.sendMessage(chatId, "Инсулин добавлен"));
                userSt.setState(State.UPDATE_START);
                userStMap.put(chatId, userSt);

            } catch (NumberFormatException e) {
                message.execute(message.sendMessage(chatId, "Инсулин указывается через точку. Пример: 1.5"));
            }
        } else {
            message.execute(message.sendMessage(chatId, "Укажите инсулин. Пример: 1.5"));
        }
    }

    public void handleWriteUpdateNote(Long chatId, String note, Map<Long, UserSt> userStMap, Message message) {
        UserSt userSt = userStMap.get(chatId);

        if (userSt == null) {
            message.execute(message.sendMessage(chatId, "Ошибка обновления. Свяжитесь с @" + Admin.getAdmin()));
            return;
        }

        UpdateSugar updateSugar = userSt.getUpdate();

        if (note != null && !note.isEmpty()) {

            updateSugar.setNote(note.trim());
            message.execute(message.sendMessage(chatId, "Заметка добавлена"));
        }

        userSt.setState(State.UPDATE_START);
        userStMap.put(chatId, userSt);
    }
}
