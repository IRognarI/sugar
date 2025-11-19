package sugar.sugar.maper;

import lombok.experimental.UtilityClass;
import sugar.sugar.dto.SugarDto;
import sugar.sugar.model.Sugar;
import sugar.sugar.util.dateTimeFormater.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@UtilityClass
public class SugarMapper {
    public static SugarDto toDto(Sugar sugar, Long id, double lastDoseOfInsulin, LocalDateTime date) {
        SugarDto sugarDto = new SugarDto();

        if (id > sugarDto.getId()) {
            sugarDto.setId(id); // id последней записи в которой было найдено совпадение по уровню сахара
        }

        if (lastDoseOfInsulin > sugarDto.getLastDoseOfInsulin()) {
            sugarDto.setLastDoseOfInsulin(lastDoseOfInsulin); // доза инсулина, которая была уколота в последний раз при совпадении по уровню сахара
        }

        String dateValue = "not found";
        if (date != null) {
            int day = date.getDayOfMonth();
            int month = date.getMonthValue();
            int year = date.getYear();

            dateValue = DateTimeFormat.dateToString(LocalDate.of(year, month, day));
        }

        sugarDto.setLastDate(dateValue);
        sugarDto.setSugarId(sugar.getId());
        sugarDto.setLevelSugar(sugar.getLevelSugar());
        sugarDto.setDoseOfInsulin(sugar.getDoseOfInsulin());
        sugarDto.setTime(DateTimeFormat.dateTimeToString(sugar.getTime()));
        sugarDto.setNote(sugarDto.getNote());

        return sugarDto;
    }
}
