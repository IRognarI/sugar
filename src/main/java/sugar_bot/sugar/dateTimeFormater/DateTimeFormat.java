package sugar_bot.sugar.dateTimeFormater;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateTimeFormat {
    private static final ZoneId zoneId = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static String dateTimeToString(LocalDateTime time) {
        return time.atZone(zoneId).format(DATE_TIME_FORMATTER);
    }

    public static String dateToString(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    public static LocalDate dateFromString(String date) {
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    public LocalDate getDateTimeFormatter(String time) {
        return LocalDate.parse(time, DATE_TIME_FORMATTER);
    }

    public LocalTime parseTime(String time) {
        return LocalTime.parse(time, TIME_FORMATTER);
    }
}
