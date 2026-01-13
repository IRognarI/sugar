package sugar.telegram.notification;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import sugar.sugar.dateTimeFormater.DateTimeFormat;
import sugar.telegram.enums.State;
import sugar.telegram.menu.Menu;
import sugar.telegram.state.UserSt;
import sugar.telegram.util.message.Message;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
@RequiredArgsConstructor
public class Notification {
    private final ZoneId zoneId = ZoneId.of("Europe/Moscow");
    private final Map<Long, Set<LocalTime>> timeMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1); // Только 1 поток!
    private final Map<Long, Set<LocalTime>> sentTimes = new ConcurrentHashMap<>();
    private final AtomicBoolean schedulerStarted = new AtomicBoolean(false);
    private final ApplicationContext context;

    // Инициализация один раз при старте приложения
    @PostConstruct
    public void init() {
        startScheduler();
    }

    private void startScheduler() {
        // Запускаем планировщик только один раз
        if (schedulerStarted.compareAndSet(false, true)) {
            executorService.scheduleAtFixedRate(this::checkAndSendNotifications, 0, 1, TimeUnit.MINUTES);
            log.info("Планировщик напоминаний запущен");
        }
    }

    public void plug(Long chatId, Message message) {
        message.execute(message.sendMessage(chatId, "Напоминание находится в разработке!"));
    }

    public void start(Long chatId, Map<Long, UserSt> userStMap, Message message) {
        UserSt userSt = new UserSt();
        userSt.setState(State.SET_TIME_FOR_NOTIFY);
        userStMap.put(chatId, userSt);
        timeMap.put(chatId, new HashSet<>()); // Используем Set вместо List
        sentTimes.put(chatId, new HashSet<>());

        message.execute(message.sendMessage(chatId, """
                Укажите время хотя бы одного напоминания
                
                Поочередно отправьте время напоминания.
                [Формат: 21:00]
                По окончанию ввода отправьте точку
                """));
    }

    public void sendNotify(Long chatId, Message message, String time, Map<Long, UserSt> userStMap) {
        UserSt userSt = userStMap.get(chatId);

        if (userSt == null) {
            message.execute(message.sendMessage(chatId, "Не найден пользователь для отправки напоминания"));
            return;
        }

        if (!Objects.equals(time, ".")) {
            try {
                LocalTime parsedTime = DateTimeFormat.parseTime(time).truncatedTo(ChronoUnit.MINUTES);

                if (timeMap.containsKey(chatId)) {
                    timeMap.get(chatId).add(parsedTime);
                    log.info("Время: {} - добавлено в напоминания пользователя с id={}", time, chatId);
                }
            } catch (DateTimeParseException e) {
                log.error(e.getMessage());
                message.execute(message.sendMessage(chatId, "Верный формат указания времени: 21:00"));
            }
        } else {
            Set<LocalTime> times = timeMap.get(chatId);
            if (times == null || times.isEmpty()) {
                message.execute(message.sendMessage(chatId, "Напоминание не установлено"));
                userStMap.remove(chatId);
            } else {
                StringBuilder sb = new StringBuilder("Напоминания сохранены:\n");
                times.forEach(t -> sb.append(t).append("\n"));
                message.execute(message.sendMessage(chatId, sb.toString()));

                // Переводим в состояние ожидания напоминаний
                userSt.setState(State.WAITING_FOR_NOTIFY);
            }
        }
    }

    private void checkAndSendNotifications() {
        LocalDateTime now = LocalDateTime.now(zoneId);
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();

        // Проходим по всем пользователям
        for (Map.Entry<Long, Set<LocalTime>> entry : timeMap.entrySet()) {
            Long chatId = entry.getKey();
            Set<LocalTime> userTimes = entry.getValue();

            if (userTimes == null || userTimes.isEmpty()) {
                continue;
            }

            // Проверяем каждое время пользователя
            for (LocalTime time : userTimes) {
                if (time.getHour() == currentHour && time.getMinute() == currentMinute) {
                    // Проверяем, не отправляли ли уже в эту минуту
                    Set<LocalTime> sentForUser = sentTimes.computeIfAbsent(chatId, k -> new HashSet<>());

                    if (!sentForUser.contains(time)) {
                        // Отправляем сообщение
                        sendNotificationMessage(chatId, time);
                        sentForUser.add(time);

                        log.debug("Отправили напоминание пользователю: {} в {}", chatId, time);
                    }
                }
            }
        }

        // Каждый час очищаем sentTimes для возможности отправки на следующий день
        if (currentMinute == 0) { // В начале каждого часа
            sentTimes.values().forEach(Set::clear);
            log.debug("Очищены sentTimes в начале часа");
        }
    }

    private void sendNotificationMessage(Long chatId, LocalTime time) {
        Message message = context.getBean(Message.class);

        message.execute(message.sendMessage(chatId, "Время: " + time + "\nВнесите запись"));
        log.info("НАПОМИНАНИЕ: пользователю {} в время {}", chatId, time);
    }

    public void disableNotify(Long chatId, Message message, Map<Long, UserSt> userStMap) {
        UserSt userSt = userStMap.get(chatId);

        if (userSt != null) {
            // Удаляем времена напоминаний
            timeMap.remove(chatId);
            sentTimes.remove(chatId);

            userStMap.remove(chatId);
            userSt.getGetNotify().getAndSet(false);

            message.execute(message.sendMessage(chatId, "Напоминания отключены"));
            Menu.sendMenu(chatId, message);
        } else {
            log.error("Пользователь с chatId {} - не найден", chatId);
            message.execute(message.sendMessage(chatId, "Напоминания не были подключены"));
        }
    }
}