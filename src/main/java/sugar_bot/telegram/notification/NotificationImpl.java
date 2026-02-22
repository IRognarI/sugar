package sugar_bot.telegram.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sugar_bot.sugar.dateTimeFormater.DateTimeFormat;
import sugar_bot.sugar.notify.Notify;
import sugar_bot.telegram.enums.State;
import sugar_bot.telegram.menu.Menu;
import sugar_bot.telegram.userCheck.UserCheck;
import sugar_bot.telegram.util.message.Message;
import sugar_bot.zoneId.TargetZoneId;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationImpl implements Notification {
    private final Notify notify;
    private final Message message;
    private final Map<Long, Set<LocalTime>> targetTimes = new ConcurrentHashMap<>();
    private final Map<Long, ZonedDateTime> lastNotify = new ConcurrentHashMap<>();

    @Override
    public void sendNotify(Long chatId, Map<Long, UserCheck> userStMap) {

        if (notify.chatIdExists(chatId)) {

            UserCheck userCheck = new UserCheck();

            userCheck.setState(State.WAIT_TIME_FOR_NOTIFY);
            userStMap.put(chatId, userCheck);
            targetTimes.put(chatId, initList(targetTimes.get(chatId)));

            message.sendMessage(chatId, sendInstruction(), null);

        } else {

            message.sendMessage(chatId, "Сначала нужно сохранить хотя бы одну запись", null);
        }
    }

    @Override
    public void setNotify(Long chatId, String times, Menu menu) {

        String text;
        if (!Objects.isNull(times)) {

            if (Objects.equals(times, ".")) {

                if (targetTimes.get(chatId).isEmpty()) {

                    text = "Нужно указать время хотя бы одного напоминания";

                    message.sendMessage(chatId, text, null);

                } else {

                    text = "Напоминания сохранены\n*Часовой пояс: МСК";

                    message.sendMessage(chatId, text + "\n" + targetTimes.get(chatId).toString() + "\n", null);
                    menu.sendMenu(chatId, message);
                }

            } else {

                try {

                    targetTimes.get(chatId).add(DateTimeFormat.parseTime(times.trim()).truncatedTo(ChronoUnit.MINUTES));

                } catch (DateTimeParseException e) {
                    log.error(e.getMessage());

                    message.sendMessage(chatId, "Время указывается в таком формате 👉 17:12", null);
                }
            }

        } else {

            message.sendMessage(chatId, "Следуйте инструкции👇\n\n" + sendInstruction(), null);
        }
    }

    @Override
    public void disableNotify(Long chatId, Map<Long, UserCheck> userStMap) {

        if (targetTimes.containsKey(chatId)) {

            targetTimes.remove(chatId);

            message.sendMessage(chatId, "Напоминания отключены", null);

        } else {
            message.sendMessage(chatId, "Напоминания не подключены", null);
        }
    }

    @Override
    public void checkMyNotify(Long chatId) {

        if (!targetTimes.containsKey(chatId) || targetTimes.get(chatId).isEmpty()) {

            message.sendMessage(chatId, "Напоминаний нет", null);

        } else {

            message.sendMessage(chatId, targetTimes.get(chatId).toString(), null);
        }
    }

    private Set<LocalTime> initList(Set<LocalTime> localTimes) {

        return localTimes == null || localTimes.isEmpty() ? new ConcurrentSkipListSet<>() : localTimes;
    }

    @Scheduled(fixedRate = 60000)
    public void checkTimeAndNotifySendActual() {
        log.debug("Запущен сервис напоминаний");

        ZonedDateTime localTime = ZonedDateTime.now(TargetZoneId.getZoneId());
        log.debug("Сейчас время: {}", zoneDateTimeToLocalTime(localTime));

        for (Long key : targetTimes.keySet()) {

            if (key != null && targetTimes.get(key).contains(zoneDateTimeToLocalTime(localTime))) {
                log.debug("Совпало время напоминания с {} у юзера {}", zoneDateTimeToLocalTime(localTime), key);

                if (lastNotify.get(key) == null || lastNotify.get(key).isBefore(localTime.truncatedTo(ChronoUnit.MINUTES))) {

                    message.sendMessage(key, "Время: " + zoneDateTimeToLocalTime(localTime) +
                            "\nВнесите запись", null);

                    lastNotify.put(key, localTime.truncatedTo(ChronoUnit.MINUTES));
                    log.debug("Для юзера {} добавили время {} в уже отправленные", key, zoneDateTimeToLocalTime(localTime));

                    log.debug("Отправили сообщение юзеру {}", key);
                }
            }
        }
    }

    private LocalTime zoneDateTimeToLocalTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES);
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
