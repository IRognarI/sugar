package sugar.telegram.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar.sugar.dateTimeFormater.DateTimeFormat;
import sugar.telegram.enums.State;
import sugar.telegram.state.UserSt;
import sugar.telegram.util.message.Message;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class Notification {
    private final ZoneId zoneId = ZoneId.of("Europe/Moscow");
    private int countTargetNotify;
    private List<LocalTime> targetNotifyTime = new ArrayList<>();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    public void plug(Long chatId, Message message) {
        message.execute(message.sendMessage(chatId, "Напоминание находится в разработке!"));
    }


    /**
    Не рабочая версия уведомлений.
    Сообщения многократно повторяются за короткий промежуток времени
    **/
    public void start(Long chatId, Map<Long, UserSt> userStMap, Message message) {
        UserSt userSt = new UserSt();

        userSt.setState(State.WAIT_TIME_FOR_NOTIFY);
        userSt.setGetNotify(true);
        userStMap.put(chatId, userSt);

        message.execute(message.sendMessage(chatId, "Укажите желаемое кол-во напоминаний в день"));
    }

    public void setNotify(Long chatId, String countNotify, Message message, Map<Long, UserSt> userStMap) {
        try {
            this.countTargetNotify = Integer.parseInt(countNotify.trim());
            log.info("Желаемое кол-во напоминай в день: {}", this.countTargetNotify);

            boolean result = checkCountNotify(chatId, countTargetNotify, message);

            if (!result) {
                return;
            }

            message.execute(message.sendMessage(chatId, "Поочередно отправьте время напоминания." +
                    " [Формат: 21:00]. По окончанию ввода отправьте точку"));

            UserSt userSt = userStMap.get(chatId);
            userSt.setState(State.SET_TIME_FOR_NOTIFY);
            userStMap.put(chatId, userSt);

        } catch (NumberFormatException e) {
            message.execute(message.sendMessage(chatId, "Укажите только одно число, например: 2"));
            log.error(e.getMessage());
        }
    }

    public void sendNotify(Long chatId, Message message, String time, Map<Long, UserSt> userStMap) {
        try {
            if (!Objects.equals(time, ".")) {
                targetNotifyTime.add(DateTimeFormat.parseTime(time).truncatedTo(ChronoUnit.MINUTES));

            } else if (targetNotifyTime.isEmpty()) {
                userStMap.remove(chatId);

            } else {
                message.execute(message.sendMessage(chatId, targetNotifyTime.size() > 1 ? "Время отправки уведомлений" +
                        " сохранено" : "Время отправки уведомления сохранено"));
            }
        } catch (DateTimeException e) {
            message.execute(message.sendMessage(chatId, "Верный формат указания времени: 21:00"));
        }

        UserSt userSt = userStMap.get(chatId);

        if (userSt != null) {
            log.debug("Пользователь для отправки напоминания - найден");

            Map<LocalTime, Boolean> flag = new ConcurrentHashMap<>();

            executorService.scheduleAtFixedRate(() -> {

                if (userSt.getGetNotify()) {
                    LocalTime now = LocalTime.now(zoneId).truncatedTo(ChronoUnit.MINUTES);

                    for (LocalTime t : targetNotifyTime) {

                        if (Objects.equals(t, now)) {

                            if (!flag.getOrDefault(t, false)) {
                                message.execute(message.sendMessage(chatId, "Время: " + t + "\nВнесите запись"));
                                log.info("Сообщение было доставлено");

                                flag.put(t, true);
                                log.debug("Установлена блокировка повторной отправки напоминания");

                                executorService.schedule(() -> {
                                    flag.remove(t);
                                    log.debug("Время: {} - снова активно", t);
                                }, 1, TimeUnit.MINUTES);
                            }
                        }
                        break;
                    }
                }
            }, 0, 60, TimeUnit.SECONDS);


        } else {
            log.info("Пользователь для отправки напоминания не найден");
            message.execute(message.sendMessage(chatId, "Напоминание не установлено"));
        }
    }

    public void disableNotify(Long chatId, Message message, Map<Long, UserSt> userStMap) {
        UserSt userSt = userStMap.get(chatId);

        if (userSt != null) {

            if (userSt.getGetNotify() == null) {
                message.execute(message.sendMessage(chatId, "Напоминания не были подключены"));

            } else {
                userSt.setGetNotify(false);
                message.execute(message.sendMessage(chatId, "Напоминания отключены"));
                userStMap.remove(chatId);
            }

        } else {
            log.error("Пользователь с chatId {} - не найден", chatId);
            message.execute(message.sendMessage(chatId, "Напоминания не были подключены"));
        }
    }

    private boolean checkCountNotify(Long chatId, int countTargetNotify, Message message) {
        if (countTargetNotify < 1) {
            log.debug("Пользователь указал не допустимое кол-во напоминаний: {}", countTargetNotify);
            message.execute(message.sendMessage(chatId, "Минимальное кол-во напоминаний: 1"));
            return false;
        } else {
            return true;
        }
    }
}
