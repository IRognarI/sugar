package sugar_bot.sugar.maper;

import lombok.experimental.UtilityClass;
import sugar_bot.sugar.dateTimeFormater.DateTimeFormat;
import sugar_bot.sugar.dto.SugarDto;
import sugar_bot.sugar.model.Sugar;

import java.util.Optional;

@UtilityClass
public class SugarMapper {
    public static SugarDto toDto(Sugar sugar, Optional<Sugar> sugarStory) {
        SugarDto sugarDto = new SugarDto();

        if (sugarStory.isPresent()) {

            if (sugarStory.get().getId() > sugarDto.getId()) {
                sugarDto.setId(sugarStory.get().getId());
            }

            if (sugarStory.get().getDoseOfInsulin() > sugarDto.getLastDoseOfInsulin()) {
                sugarDto.setLastDoseOfInsulin(sugarStory.get().getDoseOfInsulin());
            }

            sugarDto.setLastDate(DateTimeFormat.dateToString(sugarStory.get().getTime()));
        }

        sugarDto.setSugarId(sugar.getId());
        sugarDto.setLevelSugar(sugar.getLevelSugar());
        sugarDto.setDoseOfInsulin(sugar.getDoseOfInsulin());
        sugarDto.setTime(DateTimeFormat.dateTimeToString(sugar.getTime()));
        sugarDto.setNote(sugar.getNote());

        return sugarDto;
    }
}
