package sugar_bot.telegram.notification;

import sugar_bot.telegram.menu.Menu;
import sugar_bot.telegram.state.UserSt;
import sugar_bot.telegram.util.message.Message;

import java.util.Map;

public interface Notification {

    void sendNotify(Long chatId, Message message, Map<Long, UserSt> userStMap);

    void setNotify(Long chatId, String times, Message message, Menu menu, Map<Long, UserSt> userStMap);

    void disableNotify(Long chatId, Message message, Map<Long, UserSt> userStMap);

    void checkMyNotify(Long chatId, Message message);
}
