package sugar.sugar.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sugar.sugar.dto.NewSugar;
import sugar.sugar.interfaces.SugarService;
import sugar.sugar.model.Sugar;

@Service
@Transactional
public class SugarServiceImpl implements SugarService {
    @Override
    public Sugar addEntry(NewSugar newSugar) {
        return null;
    }

    @Override
    public Sugar updateEntry(String note, double doseOfInsulin) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Sugar getSugarById(Long sugarId) {
        return null;
    }

    @Override
    public void removeSugarById(Long sugarId) {

    }

    @Override
    public void clearAll() {

    }
}
