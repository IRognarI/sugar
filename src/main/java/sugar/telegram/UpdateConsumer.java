package sugar.telegram;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    public UpdateConsumer (String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    @SneakyThrows
    public void consume(Update update) {
        log.info("Пришло сообщение %s от %s".formatted(update.getMessage().getText(), update.getMessage().getChat()));

        update.getMessage().getText();
        update.getMessage().getChat();

        SendMessage message = SendMessage.builder()
                .text("Бот находится в разработке")
                .chatId(update.getMessage().getChatId())
                .build();

        telegramClient.execute(message);
    }
}
