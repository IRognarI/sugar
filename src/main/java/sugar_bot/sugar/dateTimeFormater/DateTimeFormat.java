package sugar_bot.sugar.dateTimeFormater;

import lombok.experimental.UtilityClass;
import sugar_bot.zoneId.TargetZoneId;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@UtilityClass
public class DateTimeFormat {
    private static final ZoneId zoneId = TargetZoneId.getZoneId();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static String dateTimeToString(Instant time) {

        LocalDateTime dateTime = LocalDateTime.ofInstant(time, zoneId);

        return dateTime.format(DATE_TIME_FORMATTER);
    }

    public static String dateToString(Instant date) {

        LocalDateTime dateTime = LocalDateTime.ofInstant(date, zoneId);

        return dateTime.format(DATE_FORMATTER);
    }

    public static LocalDate dateFromString(String date) {
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    public LocalDate getDateTimeFormatter(String time) {
        return LocalDate.parse(time, DATE_TIME_FORMATTER);
    }

    public LocalTime parseTime(String time) {

        return LocalTime.parse(time, TIME_FORMATTER).truncatedTo(ChronoUnit.MINUTES);
    }
}
