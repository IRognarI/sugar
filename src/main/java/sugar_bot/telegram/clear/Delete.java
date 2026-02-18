package sugar_bot.telegram.clear;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar_bot.sugar.dto.SugarDto;
import sugar_bot.sugar.exception.NotFoundException;
import sugar_bot.sugar.interfaces.SugarService;
import sugar_bot.telegram.enums.State;
import sugar_bot.telegram.menu.Menu;
import sugar_bot.telegram.userCheck.UserCheck;
import sugar_bot.telegram.util.message.Message;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class Delete {

    public void removeStart(Long chatId, Map<Long, UserCheck> userStMap, Message message) {
        UserCheck userCheck = userStMap.get(chatId);

        if (userCheck == null) {
            userCheck = new UserCheck();
        }

        log.info("Пользователь {} хочет удалить запись", chatId);

        userCheck.setState(State.REMOVE);
        userStMap.put(chatId, userCheck);

        message.sendMessage(chatId, "Отправьте ID записи, которую желаете удалить", null);

    }

    public void removeById(Long chatId, String note, SugarService sugarService, Message message, Menu menu, Map<Long, UserCheck> userStMap) {
        if (note != null && !note.isEmpty()) {
            try {
                Long sugarId = Long.parseLong(note.trim());
                log.info("Передан ID для удаления: {}", sugarId);

                SugarDto entryExists = null;
                try {
                    entryExists = sugarService.getSugarById(sugarId, chatId);

                } catch (NotFoundException e) {
                    message.sendMessage(chatId, e.getMessage(), null);
                }


                if (entryExists != null) {
                    sugarService.removeSugarById(sugarId, chatId);
                    userStMap.get(chatId).setState(State.START);
                    message.sendMessage(chatId, "Запись с ID= " + sugarId + " - была удалена", null);
                    menu.sendMenu(chatId, message);
                }

            } catch (NumberFormatException e) {
                log.debug("Указали не верный формат ID в removeById");
                message.sendMessage(chatId, "Укажите просто число. Пример: 7", null);
            }
        }
    }
}
