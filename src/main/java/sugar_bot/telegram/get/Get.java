package sugar_bot.telegram.get;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar_bot.sugar.dto.SugarDto;
import sugar_bot.sugar.exception.NotFoundException;
import sugar_bot.sugar.exception.ValidationException;
import sugar_bot.sugar.interfaces.SugarService;
import sugar_bot.telegram.enums.State;
import sugar_bot.telegram.menu.Menu;
import sugar_bot.telegram.state.UserSt;
import sugar_bot.telegram.util.message.Message;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class Get {
    public void getBySugarIdStart(Long chatId, Map<Long, UserSt> userStMap, Message message) {
        UserSt userSt = userStMap.get(chatId);

        if (userSt == null) {
            userSt = new UserSt();
        }
        userSt.setState(State.GET_SUGAR_BY_ID);
        userStMap.put(chatId, userSt);
        message.execute(message.sendMessage(chatId, "Отправьте ID записи"));
    }

    public void getSugarById(Long chatId, String note, SugarService sugarService, Message message, Map<Long, UserSt> userStMap) {
        if (note != null && !note.isEmpty()) {

            try {
                Long sugarId = Long.parseLong(note.trim());
                log.info("Пользователь {} хочет получить запись с ID {}", chatId, sugarId);

                try {
                    SugarDto sugarDto = sugarService.getSugarById(sugarId);

                    message.execute(message.sendMessage(chatId, message.answerAfterSaved(sugarDto)));
                    userStMap.remove(chatId);
                    Menu.sendMenu(chatId, message);
                } catch (ValidationException | NotFoundException e) {
                    message.execute(message.sendMessage(chatId, e.getMessage()));
                }

            } catch (NumberFormatException e) {
                log.debug("{} отправил не корректный ID записи", chatId);
                message.execute(message.sendMessage(chatId, "Отправьте просто число. Например: 7"));
            }
        } else {
            log.debug("{} отправил не корректное сообщение: {}", chatId, note);
            message.execute(message.sendMessage(chatId, "Отправьте ID записи, например: 7"));
        }
    }
}
