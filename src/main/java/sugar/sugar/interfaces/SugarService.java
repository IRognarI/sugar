package sugar.sugar.interfaces;

import sugar.sugar.dto.NewSugar;
import sugar.sugar.model.Sugar;

public interface SugarService {
    Sugar addEntry(NewSugar newSugar);

    Sugar updateEntry(String note, double doseOfInsulin);

    Sugar getSugarById(Long sugarId);

    void removeSugarById(Long sugarId);

    void clearAll();
}
