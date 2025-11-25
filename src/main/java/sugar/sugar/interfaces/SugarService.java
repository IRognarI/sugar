package sugar.sugar.interfaces;

import sugar.sugar.dto.NewSugar;
import sugar.sugar.dto.SugarDto;
import sugar.sugar.dto.UpdateSugar;

public interface SugarService {
    SugarDto addEntry(NewSugar newSugar);

    SugarDto updateEntry(UpdateSugar updateSugar);

    SugarDto getSugarById(Long sugarId);

    void removeSugarById(Long sugarId);

    void clearAll();
}
