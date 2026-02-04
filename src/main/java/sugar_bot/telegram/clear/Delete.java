package sugar_bot.telegram.clear;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar_bot.sugar.dto.SugarDto;
import sugar_bot.sugar.exception.NotFoundException;
import sugar_bot.sugar.interfaces.SugarService;
import sugar_bot.telegram.enums.State;
import sugar_bot.telegram.menu.Menu;
import sugar_bot.telegram.state.UserSt;
import sugar_bot.telegram.util.message.Message;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class Delete {

    public void removeStart(Long chatId, Map<Long, UserSt> userStMap, Message message) {
        UserSt userSt = userStMap.get(chatId);

        if (userSt == null) {
            userSt = new UserSt();
        }

        log.info("Пользователь {} хочет удалить запись", chatId);

        userSt.setState(State.REMOVE);
        userStMap.put(chatId, userSt);

        message.execute(message.sendMessage(chatId, "Отправьте ID записи, которую желаете удалить"));

    }

    public void removeById(Long chatId, String note, SugarService sugarService, Message message, Map<Long, UserSt> userStMap) {
        if (note != null && !note.isEmpty()) {
            try {
                Long sugarId = Long.parseLong(note.trim());
                log.info("Передан ID для удаления: {}", sugarId);

                SugarDto entryExists = null;
                try {
                    entryExists = sugarService.getSugarById(sugarId, chatId);

                } catch (NotFoundException e) {
                    message.execute(message.sendMessage(chatId, e.getMessage()));
                }


                if (entryExists != null) {
                    sugarService.removeSugarById(sugarId, chatId);
                    userStMap.get(chatId).setState(State.START);
                    message.execute(message.sendMessage(chatId, "Запись с ID= " + sugarId + " - была удалена"));
                    Menu.sendMenu(chatId, message);
                }

            } catch (NumberFormatException e) {
                log.debug("Указали не верный формат ID в removeById");
                message.execute(message.sendMessage(chatId, "Укажите просто число. Пример: 7"));
            }
        }
    }
}
