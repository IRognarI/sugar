package sugar.sugar.maper;

import lombok.experimental.UtilityClass;
import sugar.sugar.dateTimeFormater.DateTimeFormat;
import sugar.sugar.dto.SugarDto;
import sugar.sugar.model.Sugar;

import java.time.LocalDate;
import java.util.Optional;

@UtilityClass
public class SugarMapper {
    public static SugarDto toDto(Sugar sugar, Optional<Sugar> sugarStory) {
        SugarDto sugarDto = new SugarDto();

        if (sugarStory.isPresent()) {

            if (sugarStory.get().getId() > sugarDto.getId()) {
                // id последней записи в которой было найдено совпадение по уровню сахара
                sugarDto.setId(sugarStory.get().getId());
            }

            if (sugarStory.get().getDoseOfInsulin() > sugarDto.getLastDoseOfInsulin()) {
                // доза инсулина, которая была уколота в последний раз при совпадении по уровню сахара
                sugarDto.setLastDoseOfInsulin(sugarStory.get().getDoseOfInsulin());
            }

            int day = sugarStory.get().getTime().getDayOfMonth();
            int month = sugarStory.get().getTime().getMonthValue();
            int year = sugarStory.get().getTime().getYear();

            sugarDto.setLastDate(DateTimeFormat.dateToString(LocalDate.of(year, month, day)));
        }

        sugarDto.setSugarId(sugar.getId());
        sugarDto.setLevelSugar(sugar.getLevelSugar());
        sugarDto.setDoseOfInsulin(sugar.getDoseOfInsulin());
        sugarDto.setTime(DateTimeFormat.dateTimeToString(sugar.getTime()));
        sugarDto.setNote(sugar.getNote());

        return sugarDto;
    }
}
