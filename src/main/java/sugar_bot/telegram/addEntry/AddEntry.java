package sugar_bot.telegram.addEntry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar_bot.sugar.dto.NewSugar;
import sugar_bot.sugar.dto.SugarDto;
import sugar_bot.sugar.interfaces.SugarService;
import sugar_bot.telegram.enums.State;
import sugar_bot.telegram.menu.Menu;
import sugar_bot.telegram.userCheck.UserCheck;
import sugar_bot.telegram.util.message.Message;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class AddEntry {
    public void start(Long chatId, Message message, Map<Long, UserCheck> userStMap) {
        UserCheck userCheck = new UserCheck();
        NewSugar newSugar = new NewSugar();
        userCheck.setNewSugar(newSugar);
        userCheck.getNewSugar().setChatId(chatId);

        message.sendMessage(chatId, "Введите сахар", null);

        userCheck.setState(State.SUGAR);

        userStMap.put(chatId, userCheck);
    }

    public void handleWriteSugar(Long chatId, String note, Map<Long, UserCheck> userStMap, Message message) {
        UserCheck userCheck = userStMap.get(chatId);

        if (note != null && !note.isEmpty()) {

            try {
                double sugarLevel = Double.parseDouble(note.trim());
                userCheck.getNewSugar().setSugarLevel(sugarLevel);
                log.info("ChatId в userSt.getNewSugar() == {}", userCheck.getNewSugar().getChatId());
                log.debug("Записали сахар в handleWriteSugar: {}", userCheck.getNewSugar().getSugarLevel());

                message.sendMessage(chatId, "Укажите дозу инсулина 👇 Если указывать не нужно, отправьте точку", null);
                userCheck.setState(State.INSULIN);

                userStMap.put(chatId, userCheck);
            } catch (NumberFormatException e) {
                log.debug("Выброшено исключение NumberFormatException в handleWriteSugar");
                message.sendMessage(chatId, "Уровень сахара указывается через точку. Пример: 9.2", null);
            }

        } else {
            log.debug("Не указали уровень сахара");
            message.sendMessage(chatId, "Уровень сахара должен быть указан", null);
        }
    }

    public void handleWriteInsulin(Long chatId, String note, Map<Long, UserCheck> userStMap, Message message) {
        UserCheck userCheck = userStMap.get(chatId);

        if (note != null && !note.isEmpty() && !note.equals(".")) {

            try {
                double insulin = Double.parseDouble(note.trim());
                userCheck.getNewSugar().setDoseOfInsulin(insulin);
                log.debug("Записали инсулин в handleWriteInsulin: {}", userCheck.getNewSugar().getDoseOfInsulin());

                message.sendMessage(chatId, "Можно добавить заметку 📝. Если не нужна введите точку", null);
                userCheck.setState(State.NOTE);

                userStMap.put(chatId, userCheck);
            } catch (NumberFormatException e) {
                log.debug("Выброшен NumberFormatException в handleWriteInsulin");
                message.sendMessage(chatId, "Инсулин указывается через точку. Пример: 1.5", null);
            }

        } else {
            message.sendMessage(chatId, "Инсулин остается: " + userCheck.getNewSugar().getDoseOfInsulin(), null);

            message.sendMessage(chatId, "Можно добавить заметку 📝. Если не нужна введите точку", null);
            userCheck.setState(State.NOTE);

            userStMap.put(chatId, userCheck);
        }
    }

    public void handleWriteNote(Long chatId, String note, Map<Long, UserCheck> userStMap, Message message, Menu menu, SugarService sugarService) {
        UserCheck userCheck = userStMap.get(chatId);

        if (note != null && !note.isEmpty() && !note.equals(".")) {

            userCheck.getNewSugar().setNote(note.trim());
            log.debug("Новая заметка {}", userCheck.getNewSugar().getNote());
        } else {
            message.sendMessage(chatId, "Заметка остается по умолчанию: " + userCheck.getNewSugar().getNote(), null);
        }

        SugarDto sugarDto = sugarService.addEntry(userCheck.getNewSugar());
        message.sendMessage(chatId, message.answerAfterSaved(sugarDto), null);

        userStMap.get(chatId).setState(State.START);
        menu.sendMenu(chatId, message);
    }
}
