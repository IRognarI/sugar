package sugar_bot.telegram.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar_bot.sugar.dateTimeFormater.DateTimeFormat;
import sugar_bot.sugar.notify.Notify;
import sugar_bot.telegram.enums.State;
import sugar_bot.telegram.menu.Menu;
import sugar_bot.telegram.state.UserSt;
import sugar_bot.telegram.util.message.Message;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationImpl implements Notification {
    private final Notify notify;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Map<Long, List<LocalTime>> targetTimes = new ConcurrentHashMap<>();
    private final Map<Long, List<LocalTime>> lastNotify = new ConcurrentHashMap<>();
    private final Object obj = new Object();

    @Override
    public void sendNotify(Long chatId, Message message, Map<Long, UserSt> userStMap) {

        if (notify.chatIdExists(chatId)) {

            UserSt userSt = new UserSt();
            userSt.setGetNotify(true);

            userSt.setState(State.WAIT_TIME_FOR_NOTIFY);
            userStMap.put(chatId, userSt);
            targetTimes.put(chatId, initList(targetTimes.get(chatId)));
            lastNotify.put(chatId, initList(lastNotify.get(chatId)));

            message.execute(message.sendMessage(chatId, sendInstruction()));

        } else {

            message.execute(message.sendMessage(chatId, "Сначала нужно сохранить хотя бы одну запись"));
        }
    }

    @Override
    public void setNotify(Long chatId, String times, Message message, Map<Long, UserSt> userStMap) {

        String text;
        if (!Objects.isNull(times)) {

            if (Objects.equals(times, ".")) {

                if (targetTimes.get(chatId).isEmpty()) {

                    text = "Нужно указать время хотя бы одного напоминания";

                    message.execute(message.sendMessage(chatId, text));

                } else {

                    text = "Напоминания сохранены";

                    message.execute(message.sendMessage(chatId, text + "\n" + targetTimes.get(chatId) + "\n"));
                    Menu.sendMenu(chatId, message);
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
    public void disableNotify(Long chatId, Message message, Map<Long, UserSt> userStMap) {
        UserSt userSt = userStMap.get(chatId);

        if (userSt != null && userSt.isGetNotify()) {

            userSt.setGetNotify(false);
            targetTimes.remove(chatId);
            userStMap.put(chatId, userSt);

            message.execute(message.sendMessage(chatId, "Напоминания отключены"));

        } else {
            message.execute(message.sendMessage(chatId, "Напоминания не были подключены"));
        }
    }

    private List<LocalTime> initList(List<LocalTime> localTimes) {

        return localTimes == null || localTimes.isEmpty() ? new CopyOnWriteArrayList<>() : localTimes;
    }

    private void checkTimeAndNotifySend(Long chatId, Message message, Map<Long, UserSt> userStMap) {

        synchronized (obj) {

            UserSt userSt = userStMap.get(chatId);

            if (userSt != null && userSt.isGetNotify()) {
                log.debug("Уведомления разрешены");

                LocalTime timeIsNow = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);

                List<LocalTime> time = targetTimes.get(chatId);

                for (LocalTime t : time) {

                    if (Objects.equals(timeIsNow, t)) {

                        if (!lastNotify.get(chatId).contains(t)) {

                            message.execute(message.sendMessage(chatId, "Время: " + t + "\nВнесите запись"));
                            lastNotify.get(chatId).add(t);

                            log.debug("Отправили сообщение пользователю: {}", chatId);
                            break;
                        }
                    }
                }

                executor.scheduleAtFixedRate(() -> lastNotify.get(chatId).clear(), 1, 1, TimeUnit.HOURS);
            }
        }
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
