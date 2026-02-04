package sugar_bot.sugar.interfaces;

import sugar_bot.sugar.dto.NewSugar;
import sugar_bot.sugar.dto.SugarDto;
import sugar_bot.sugar.dto.UpdateSugar;

import java.time.Instant;
import java.util.List;

public interface SugarService {
    SugarDto addEntry(NewSugar newSugar);

    SugarDto updateEntry(UpdateSugar updateSugar);

    SugarDto getSugarById(Long sugarId, Long chatId);

    void removeSugarById(Long sugarId, Long chatId);

    List<SugarDto> getSugarBetweenPeriod(Instant start, Instant end, Long chatId);

}
