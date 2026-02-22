package sugar_bot.telegram.notification;

import sugar_bot.telegram.menu.Menu;
import sugar_bot.telegram.userCheck.UserCheck;

import java.util.Map;

public interface Notification {

    void sendNotify(Long chatId, Map<Long, UserCheck> userStMap);

    void setNotify(Long chatId, String times, Menu menu);

    void disableNotify(Long chatId, Map<Long, UserCheck> userStMap);

    void checkMyNotify(Long chatId);
}
