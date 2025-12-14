package sugar.telegram.tg_default;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import sugar.telegram.update.UpdateConsumer;

@Component
@RequiredArgsConstructor
public class TelegramBot implements SpringLongPollingBot {

    private final UpdateConsumer updateConsumer;


    @Override
    public String getBotToken() {
        return System.getenv("TELEGRAM_BOT_TOKEN");
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
