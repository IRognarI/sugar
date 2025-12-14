package sugar.telegram.loger;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Logger {
    private Long chatId;
    private String message;
    private LocalDateTime dateTime;

    public Logger(Long chatId, String message) {
        this.chatId = chatId;
        this.message = message;
        this.dateTime = LocalDateTime.now();
    }
}
