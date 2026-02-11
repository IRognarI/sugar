package sugar_bot.telegram.menu;

import sugar_bot.telegram.util.message.Message;

public interface Menu {

    void sendMenu(Long chatId, Message message);
}
