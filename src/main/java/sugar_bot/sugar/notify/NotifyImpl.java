package sugar_bot.sugar.notify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar_bot.sugar.repository.SugarRepository;

@RequiredArgsConstructor
@Component
@Slf4j
public class NotifyImpl implements Notify {
    private final SugarRepository sugarRepository;

    @Override
    public boolean chatIdExists(Long chatId) {
        log.info("Передан chatId для проверки: {}", chatId);

        return sugarRepository.existsByChatId(chatId);
    }
}
