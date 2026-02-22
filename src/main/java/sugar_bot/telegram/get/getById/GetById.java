package sugar_bot.telegram.get.getById;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar_bot.sugar.dto.SugarDto;
import sugar_bot.sugar.exception.NotFoundException;
import sugar_bot.sugar.exception.ValidationException;
import sugar_bot.sugar.interfaces.SugarService;
import sugar_bot.telegram.enums.State;
import sugar_bot.telegram.menu.Menu;
import sugar_bot.telegram.userCheck.UserCheck;
import sugar_bot.telegram.util.message.Message;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetById {
    public void getBySugarIdStart(Long chatId, Map<Long, UserCheck> userStMap, Message message) {
        UserCheck userCheck = userStMap.get(chatId);

        if (userCheck == null) {
            userCheck = new UserCheck();
        }
        userCheck.setState(State.GET_SUGAR_BY_ID);
        userStMap.put(chatId, userCheck);
        message.sendMessage(chatId, "Отправьте ID записи", null);
    }

    public void getSugarById(Long chatId, String note, SugarService sugarService, Message message, Menu menu, Map<Long, UserCheck> userStMap) {
        if (note != null && !note.isEmpty()) {

            try {
                Long sugarId = Long.parseLong(note.trim());
                log.info("Пользователь {} хочет получить запись с ID {}", chatId, sugarId);

                try {
                    SugarDto sugarDto = sugarService.getSugarById(sugarId, chatId);

                    message.sendMessage(chatId, message.answerAfterSaved(sugarDto), null);
                    userStMap.get(chatId).setState(State.START);
                    menu.sendMenu(chatId, message);
                } catch (ValidationException | NotFoundException e) {
                    message.sendMessage(chatId, e.getMessage(), null);
                }

            } catch (NumberFormatException e) {
                log.debug("{} отправил не корректный ID записи", chatId);
                message.sendMessage(chatId, "Отправьте просто число. Например: 7", null);
            }
        } else {
            log.debug("{} отправил не корректное сообщение: {}", chatId, note);
            message.sendMessage(chatId, "Отправьте ID записи, например: 7", null);
        }
    }
}
