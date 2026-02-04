package sugar_bot.telegram.get.getForPeriod;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sugar_bot.sugar.dateTimeFormater.DateTimeFormat;
import sugar_bot.sugar.dto.SugarDto;
import sugar_bot.sugar.exception.ValidationException;
import sugar_bot.sugar.interfaces.SugarService;
import sugar_bot.telegram.enums.State;
import sugar_bot.telegram.state.UserSt;
import sugar_bot.telegram.util.message.Message;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class GetForPeriod {

    private final SugarService service;

    private final List<LocalDateTime> dateList = new ArrayList<>();

    public GetForPeriod(SugarService service) {
        this.service = service;
    }

    public void requestPeriod(Long chatId, Message message, Map<Long, UserSt> userStMap) {

        UserSt userSt = userStMap.get(chatId);

        if (userSt == null) {
            userSt = new UserSt();
        }

        userSt.setState(State.WAITING_FOR_DATES);

        userStMap.put(chatId, userSt);

        message.execute(message.sendMessage(chatId, sendInstruction()));
    }

    public void returnEntryList(Long chatId, String val, Message message, Map<Long, UserSt> userStMap) {
        log.debug("Готовимся отдать список");

        try {

            if (dateList.size() != 2 && !Objects.equals(val, ".")) {

                dateList.add(DateTimeFormat.dateFromString(val).atStartOfDay());

                log.debug("Кол-во добавленных дат для фильтрации: {}", dateList.size());

            } else {

                log.debug("Получили кол-во дат для поиска записей: {}", dateList.size());

                whenBothDatesAreReceived(dateList, message, chatId);

                dateList.clear();
                userStMap.get(chatId).setState(State.START);
            }

        } catch (DateTimeParseException e) {
            log.error(e.getMessage());

            message.execute(message.sendMessage(chatId, "Введите верный формат даты: 23.12.2025\nДаты нужно" +
                    " отправлять по очереди"));
        }

    }

    private void whenBothDatesAreReceived(List<LocalDateTime> dateList, Message message, Long chatId) {

        try {

            LocalDateTime first = dateList.get(0);
            LocalDateTime second = dateList.get(1);

            LocalDateTime start, end;

            if (first.isBefore(second)) {

                start = first;
                end = second;

            } else {

                start = second;
                end = first;
            }

            log.debug("Получили дату начала поиска и конца: {}, {}", start, end);

            List<SugarDto> sugarDtosList = service.getSugarBetweenPeriod(start.toInstant(ZoneOffset.UTC), end.toInstant(ZoneOffset.UTC), chatId);

            log.debug("Кол-во найденных записей: {}", sugarDtosList.size());

            if (sugarDtosList.isEmpty()) {

                message.execute(message.sendMessage(chatId, "Записи за указанный период не найдены"));

            } else {

                for (SugarDto dto : sugarDtosList) {

                    message.execute(message.sendMessage(chatId, message.answerAfterSaved(dto)));
                }

                log.info("Отправили записи пользователю {}", chatId);
            }

        } catch (ValidationException e) {
            message.execute(message.sendMessage(chatId, e.getMessage()));
        }
    }

    private String sendInstruction() {

        return """
                1️⃣ Отправьте дату начала поиска
                2️⃣ После - дату окончания поиска
                
                🔸 По окончании ввода дат, отправьте точку
                
                ✅ Ожидаемый формат даты: 23.12.2025
                """;
    }
}
