package sugar.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfiguration {

    @Value("${telegram.bot.token}")
    private String token;

    @Bean
    public UpdateConsumer updateConsumer() {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("TELEGRAM_BOT_TOKEN не задан! Укажите в application.properties или через переменную окружения TELEGRAM_BOT_TOKEN");
        }

        return new UpdateConsumer(token);
    }
}
