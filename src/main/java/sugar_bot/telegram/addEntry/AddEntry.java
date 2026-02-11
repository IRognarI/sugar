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

        message.execute(message.sendMessage(chatId, "Введите сахар"));

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

                message.execute(message.sendMessage(chatId, "Укажите дозу инсулина 👇 Если указывать не нужно, отправьте точку"));
                userCheck.setState(State.INSULIN);

                userStMap.put(chatId, userCheck);
            } catch (NumberFormatException e) {
                log.debug("Выброшено исключение NumberFormatException в handleWriteSugar");
                message.execute(message.sendMessage(chatId, "Уровень сахара указывается через точку. Пример: 9.2"));
            }

        } else {
            log.debug("Не указали уровень сахара");
            message.execute(message.sendMessage(chatId, "Уровень сахара должен быть указан"));
        }
    }

    public void handleWriteInsulin(Long chatId, String note, Map<Long, UserCheck> userStMap, Message message) {
        UserCheck userCheck = userStMap.get(chatId);

        if (note != null && !note.isEmpty() && !note.equals(".")) {

            try {
                double insulin = Double.parseDouble(note.trim());
                userCheck.getNewSugar().setDoseOfInsulin(insulin);
                log.debug("Записали инсулин в handleWriteInsulin: {}", userCheck.getNewSugar().getDoseOfInsulin());

                message.execute(message.sendMessage(chatId, "Можно добавить заметку 📝. Если не нужна введите точку"));
                userCheck.setState(State.NOTE);

                userStMap.put(chatId, userCheck);
            } catch (NumberFormatException e) {
                log.debug("Выброшен NumberFormatException в handleWriteInsulin");
                message.execute(message.sendMessage(chatId, "Инсулин указывается через точку. Пример: 1.5"));
            }

        } else {
            message.execute(message.sendMessage(chatId, "Инсулин остается: " + userCheck.getNewSugar().getDoseOfInsulin()));

            message.execute(message.sendMessage(chatId, "Можно добавить заметку 📝. Если не нужна введите точку"));
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
            message.execute(message.sendMessage(chatId, "Заметка остается по умолчанию: " + userCheck.getNewSugar().getNote()));
        }

        SugarDto sugarDto = sugarService.addEntry(userCheck.getNewSugar());
        message.execute(message.sendMessage(chatId, message.answerAfterSaved(sugarDto)));

        userStMap.get(chatId).setState(State.START);
        menu.sendMenu(chatId, message);
    }
}
