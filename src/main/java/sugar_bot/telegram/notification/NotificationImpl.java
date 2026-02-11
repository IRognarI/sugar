package sugar_bot.telegram.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar_bot.sugar.dateTimeFormater.DateTimeFormat;
import sugar_bot.sugar.notify.Notify;
import sugar_bot.telegram.enums.State;
import sugar_bot.telegram.menu.Menu;
import sugar_bot.telegram.userCheck.UserCheck;
import sugar_bot.telegram.util.message.Message;
import sugar_bot.zoneId.TargetZoneId;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationImpl implements Notification {
    private final Notify notify;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Map<Long, Set<LocalTime>> targetTimes = new ConcurrentHashMap<>();
    private final Map<Long, Set<LocalTime>> lastNotify = new ConcurrentHashMap<>();
    private final Object obj = new Object();

    @Override
    public void sendNotify(Long chatId, Message message, Map<Long, UserCheck> userStMap) {

        if (notify.chatIdExists(chatId)) {

            UserCheck userCheck = new UserCheck();
            userCheck.setGetNotify(true);

            userCheck.setState(State.WAIT_TIME_FOR_NOTIFY);
            userStMap.put(chatId, userCheck);
            targetTimes.put(chatId, initList(targetTimes.get(chatId)));
            lastNotify.put(chatId, initList(lastNotify.get(chatId)));

            message.execute(message.sendMessage(chatId, sendInstruction()));

        } else {

            message.execute(message.sendMessage(chatId, "Сначала нужно сохранить хотя бы одну запись"));
        }
    }

    @Override
    public void setNotify(Long chatId, String times, Message message, Menu menu, Map<Long, UserCheck> userStMap) {

        String text;
        if (!Objects.isNull(times)) {

            if (Objects.equals(times, ".")) {

                if (targetTimes.get(chatId).isEmpty()) {

                    text = "Нужно указать время хотя бы одного напоминания";

                    message.execute(message.sendMessage(chatId, text));

                } else {

                    text = "Напоминания сохранены\n*Часовой пояс: МСК";

                    message.execute(message.sendMessage(chatId, text + "\n" + targetTimes.get(chatId).toString() + "\n"));
                    menu.sendMenu(chatId, message);
                }

                executor.scheduleAtFixedRate(() -> checkTimeAndNotifySend(chatId, message, userStMap), 0, 1, TimeUnit.MINUTES);

            } else {

                try {

                    targetTimes.get(chatId).add(DateTimeFormat.parseTime(times.trim()).truncatedTo(ChronoUnit.MINUTES));

                } catch (DateTimeParseException e) {
                    log.error(e.getMessage());

                    message.execute(message.sendMessage(chatId, "Время указывается в таком формате 👉 17:12"));
                }
            }

        } else {

            message.execute(message.sendMessage(chatId, "Следуйте инструкции👇\n\n" + sendInstruction()));
        }
    }

    @Override
    public void disableNotify(Long chatId, Message message, Map<Long, UserCheck> userStMap) {
        UserCheck userCheck = userStMap.get(chatId);

        if (userCheck != null && userCheck.isGetNotify()) {

            userCheck.setGetNotify(false);
            targetTimes.remove(chatId);
            userStMap.put(chatId, userCheck);

            message.execute(message.sendMessage(chatId, "Напоминания отключены"));

        } else {
            message.execute(message.sendMessage(chatId, "Напоминания не были подключены"));
        }
    }

    @Override
    public void checkMyNotify(Long chatId, Message message) {

        if (!targetTimes.containsKey(chatId) || targetTimes.get(chatId).isEmpty()) {

            message.execute(message.sendMessage(chatId, "Напоминаний нет"));

        } else {

            message.execute(message.sendMessage(chatId, targetTimes.get(chatId).toString()));
        }
    }

    private Set<LocalTime> initList(Set<LocalTime> localTimes) {

        return localTimes == null || localTimes.isEmpty() ? new ConcurrentSkipListSet<>() : localTimes;
    }

    private void checkTimeAndNotifySend(Long chatId, Message message, Map<Long, UserCheck> userStMap) {

        synchronized (obj) {

            UserCheck userCheck = userStMap.get(chatId);

            if (userCheck != null && userCheck.isGetNotify()) {
                log.debug("Напоминания разрешены");

                Instant timeIsNow = Instant.now();

                ZonedDateTime zoneTime = instantToZoneDateTime(timeIsNow);
                log.debug("Сейчас время: {}", zoneDateTimeToLocalTime(zoneTime));

                Set<LocalTime> time = targetTimes.get(chatId);

                for (LocalTime t : time) {

                    LocalDateTime userTime = LocalDateTime.of(LocalDate.now(), t);

                    ZonedDateTime zoneUserTime = ZonedDateTime.of(userTime, TargetZoneId.getZoneId());

                    boolean timeIsSend = nowEqualsNotifyTime(zoneTime, zoneUserTime);

                    log.debug("zoneTime ({}) и zoneUserTime ({}) равны: {}",
                            zoneTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES),
                            zoneUserTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES), timeIsSend);

                    if (timeIsSend) {

                        if (!lastNotify.get(chatId).contains(zoneDateTimeToLocalTime(zoneUserTime))) {

                            message.execute(message.sendMessage(chatId, "Время: " + zoneDateTimeToLocalTime(zoneUserTime) + "\nВнесите запись"));
                            lastNotify.get(chatId).add(zoneDateTimeToLocalTime(zoneUserTime));

                            log.debug("Отправили сообщение пользователю: {}", chatId);
                            break;
                        }
                    }
                }

                executor.scheduleAtFixedRate(() -> lastNotify.get(chatId).clear(), 0, 1, TimeUnit.HOURS);
            }
        }
    }

    private ZonedDateTime instantToZoneDateTime(Instant instant) {
        return ZonedDateTime.ofInstant(instant, TargetZoneId.getZoneId());
    }

    private LocalTime zoneDateTimeToLocalTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES);
    }

    private boolean nowEqualsNotifyTime(ZonedDateTime now, ZonedDateTime target) {
        return now.toLocalTime().truncatedTo(ChronoUnit.MINUTES).equals(target.toLocalTime().truncatedTo(ChronoUnit.MINUTES));
    }

    private String sendInstruction() {
        return """
                ⏰Укажите время напоминания
                
                ❗Время нужно отправлять поочередно.
                
                ⚠️Так нельзя: 07:30, 18:12
                
                ✅Так можно: 07:30 ...после отправьте следующее время (если нужно несколько напоминаний в сутки)
                
                📍Для сохранения времени напоминаний - отправьте точку
                """;
    }
}
