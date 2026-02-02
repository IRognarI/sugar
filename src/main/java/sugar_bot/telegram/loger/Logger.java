package sugar_bot.telegram.loger;

import lombok.Data;

import java.time.Instant;

@Data
public class Logger {
    private Long chatId;
    private String message;
    private Instant dateTime;

    public Logger(Long chatId, String message) {
        this.chatId = chatId;
        this.message = message;
        this.dateTime = Instant.now();
    }
}
