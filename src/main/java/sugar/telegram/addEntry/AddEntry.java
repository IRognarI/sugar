package sugar.telegram.addEntry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar.sugar.dto.NewSugar;
import sugar.sugar.dto.SugarDto;
import sugar.sugar.interfaces.SugarService;
import sugar.telegram.enums.State;
import sugar.telegram.menu.Menu;
import sugar.telegram.state.UserSt;
import sugar.telegram.util.message.Message;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class AddEntry {
    public void start(Long chatId, Message message, Map<Long, UserSt> userStMap) {
        UserSt userSt = new UserSt();
        NewSugar newSugar = new NewSugar();
        userSt.setNewSugar(newSugar);

        message.execute(message.sendMessage(chatId, "Введите сахар"));

        userSt.setState(State.SUGAR);

        userStMap.put(chatId, userSt);
    }

    public void handleWriteSugar(Long chatId, String note, Map<Long, UserSt> userStMap, Message message) {
        UserSt userSt = userStMap.get(chatId);

        if (note != null && !note.isEmpty()) {

            try {
                double sugarLevel = Double.parseDouble(note.trim());
                userSt.getNewSugar().setSugarLevel(sugarLevel);
                log.debug("Записали сахар в handleWriteSugar: {}", userSt.getNewSugar().getSugarLevel());

                message.execute(message.sendMessage(chatId, "Укажите дозу инсулина 👇 Если указывать не нужно, отправьте точку"));
                userSt.setState(State.INSULIN);

                userStMap.put(chatId, userSt);
            } catch (NumberFormatException e) {
                log.debug("Выброшено исключение NumberFormatException в handleWriteSugar");
                message.execute(message.sendMessage(chatId, "Уровень сахара указывается через точку. Пример: 9.2"));
            }

        } else {
            log.debug("Не указали уровень сахара");
            message.execute(message.sendMessage(chatId, "Уровень сахара должен быть указан"));
        }
    }

    public void handleWriteInsulin(Long chatId, String note, Map<Long, UserSt> userStMap, Message message) {
        UserSt userSt = userStMap.get(chatId);

        if (note != null && !note.isEmpty() && !note.equals(".")) {


            try {
                double insulin = Double.parseDouble(note.trim());
                userSt.getNewSugar().setDoseOfInsulin(insulin);
                log.debug("Записали инсулин в handleWriteInsulin: {}", userSt.getNewSugar().getDoseOfInsulin());

                message.execute(message.sendMessage(chatId, "Можно добавить заметку 📝. Если не нужна введите точку"));
                userSt.setState(State.NOTE);

                userStMap.put(chatId, userSt);
            } catch (NumberFormatException e) {
                log.debug("Выброшен NumberFormatException в handleWriteInsulin");
                message.execute(message.sendMessage(chatId, "Инсулин указывается через точку. Пример: 1.5"));
            }

        } else {
            message.execute(message.sendMessage(chatId, "Инсулин остается: " + userSt.getNewSugar().getDoseOfInsulin()));

            message.execute(message.sendMessage(chatId, "Можно добавить заметку 📝. Если не нужна введите точку"));
            userSt.setState(State.NOTE);

            userStMap.put(chatId, userSt);
        }
    }

    public void handleWriteNote(Long chatId, String note, Map<Long, UserSt> userStMap, Message message, SugarService sugarService) {
        UserSt userSt = userStMap.get(chatId);

        if (note != null && !note.isEmpty() && !note.equals(".")) {

            userSt.getNewSugar().setNote(note.trim());
            log.debug("Новая заметка {}", userSt.getNewSugar().getNote());
        } else {
            message.execute(message.sendMessage(chatId, "Заметка остается по умолчанию: " + userSt.getNewSugar().getNote()));
        }

        SugarDto sugarDto = sugarService.addEntry(userStMap.get(chatId).getNewSugar());
        message.execute(message.sendMessage(chatId, message.answerAfterSaved(sugarDto)));

        userStMap.remove(chatId);
        Menu.sendMenu(chatId, message);
    }
}
