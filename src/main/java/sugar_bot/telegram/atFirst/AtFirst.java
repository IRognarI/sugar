package sugar_bot.telegram.atFirst;

import org.springframework.stereotype.Component;
import sugar_bot.telegram.enums.State;
import sugar_bot.telegram.userCheck.UserCheck;
import sugar_bot.telegram.util.message.Message;

import java.util.Map;

@Component
public class AtFirst {

    public void atFirst(Long chatId, Message message, Map<Long, UserCheck> userCheckMap) {

        UserCheck userCheck = userCheckMap.get(chatId);

        if (userCheck != null) {

            userCheck.setState(State.START);

            userCheckMap.put(chatId, userCheck);

            message.execute(message.sendMessage(chatId, "👉 /start"));
        }
    }
}
