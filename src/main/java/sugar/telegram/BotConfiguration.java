package sugar.telegram;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfiguration {

    @Bean
    public UpdateConsumer  updateConsumer() {
        String token = System.getenv("TELEGRAM_BOT_TOKEN");

        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException(token + " не задан!");
        }

        return new UpdateConsumer(token);
    }
}
