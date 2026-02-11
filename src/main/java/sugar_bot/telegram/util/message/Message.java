package sugar_bot.telegram.util.message;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import sugar_bot.sugar.dto.SugarDto;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
@Validated
public class Message {
    private final TelegramClient telegramClient;

    public SendMessage sendMessage(Long chatId, @NotNull(message = "Сообщение не может быть null") String message) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();
    }

    public void execute(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Сообщение не было отправлено: {}", e.getMessage());
        }
    }

    public String answerAfterSaved(SugarDto sugarDto) {

        return format("Предыдущая запись:%n%nID прошлой записи: %d%nДоза инсулина при сахаре: %.1f - %.2f%n" +
                        "Последний раз, когда сахар был: %.1f - %s%n%nТекущая запись:%n%nID: %d%nСахар: %.1f%nИнсулин: %.2f%nВремя: %s%nЗаметка: %s",
                sugarDto.getId(), sugarDto.getLevelSugar(), sugarDto.getLastDoseOfInsulin(), sugarDto.getLevelSugar(),
                sugarDto.getLastDate(), sugarDto.getSugarId(), sugarDto.getLevelSugar(), sugarDto.getDoseOfInsulin(),
                sugarDto.getTime(), sugarDto.getNote());
    }
}
