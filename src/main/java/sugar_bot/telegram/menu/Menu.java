package sugar_bot.telegram.menu;

import sugar_bot.telegram.util.message.Message;

public interface Menu {

    public void sendMenu(Long chatId, Message message);
}
