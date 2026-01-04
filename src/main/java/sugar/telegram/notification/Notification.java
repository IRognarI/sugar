package sugar.telegram.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar.telegram.util.message.Message;

import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class Notification {
    private static final LocalTime morningTime = LocalTime.of(7, 30);
    private static final LocalTime eveningTime = LocalTime.of(20, 30);
    private static final Long periodCheck = 60L;
    private static final Long initialDelay = 15L;
    private static final TimeUnit timeUnit = TimeUnit.SECONDS;
    public void sendNotification(Set<Long> setChatId, String userName, Message message) {

        if (!setChatId.isEmpty()) {
            log.debug("Зашли в sendNotification");

            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

            executorService.scheduleAtFixedRate(() -> {
                log.debug("Внутри дочернего потока");

                LocalTime now = LocalTime.now();

                int hour = now.getHour();
                int minute = now.getMinute();

                if ((hour == morningTime.getHour() && minute == morningTime.getMinute()) ||
                        (hour == eveningTime.getHour() && minute == eveningTime.getMinute())) {

                    for (Long id : setChatId) {
                        log.info("Отправили сообщение пользователю: " + id);

                        String text = userName != null ? userName + ", не забудьте внести запись" :
                                "Не забудьте внести запись";

                        message.execute(message.sendMessage(id, text));
                    }
                }

            }, initialDelay, periodCheck, timeUnit);

        } else {
            log.info("setChatId пустой");
        }
    }
}
