package sugar_bot.sugar.interfaces;

import sugar_bot.sugar.dto.NewSugar;
import sugar_bot.sugar.dto.SugarDto;
import sugar_bot.sugar.dto.UpdateSugar;

public interface SugarService {
    SugarDto addEntry(NewSugar newSugar);

    SugarDto updateEntry(UpdateSugar updateSugar);

    SugarDto getSugarById(Long sugarId);

    void removeSugarById(Long sugarId);

    void clearAll();
}
