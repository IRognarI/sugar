package sugar.telegram.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import sugar.telegram.util.message.Message;

@Configuration
public class BotConfiguration {

    @Value("${telegram.bot.token}")
    private String token;

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(token);
    }

    @Bean
    public Message message(TelegramClient telegramClient) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("TELEGRAM_BOT_TOKEN не задан! Укажите в application.properties или через переменную окружения TELEGRAM_BOT_TOKEN");
        }

        return new Message(telegramClient);
    }
}
