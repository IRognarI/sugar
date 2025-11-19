package sugar.sugar.util.dateTimeFormater;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateTimeFormat {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static String dateTimeToString(LocalDateTime time) {
        return time.format(DATE_TIME_FORMATTER);
    }

    public static String dateToString(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
}
