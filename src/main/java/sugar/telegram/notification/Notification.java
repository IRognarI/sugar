package sugar.telegram.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar.telegram.util.message.Message;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class Notification {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final ZoneId zoneId = ZoneId.of("Europe/Moscow");
    private final LocalTime morningTime = LocalTime.of(7, 32);
    private final LocalTime eveningTime = LocalTime.of(19, 32);

    public void sendNotification(Set<Long> setChatId, String userName, Message message) {

        if (setChatId.isEmpty()) {
            log.info("setChatId is empty");
            return;
        }

        log.info("Кол-во user для получения уведомления: {}", setChatId.size());

        executorService.scheduleAtFixedRate(() -> {

            LocalTime now = LocalTime.now(zoneId).truncatedTo(ChronoUnit.MINUTES);
            boolean timeIsMessage = now.equals(morningTime) || now.equals(eveningTime);
            log.debug("LocalTimeNow ({}) == morningTime ({}) or eveningTime ({}): {}", now, morningTime, eveningTime, timeIsMessage);

            if (timeIsMessage) {

                for (Long id : setChatId) {

                    message.execute(message.sendMessage(id, userName != null ? userName + ", не забудьте внести запись" :
                            "Не забудьте внести запись"));
                }

                executorService.shutdown();
            }
        }, 0, 60, TimeUnit.SECONDS);
    }
}
