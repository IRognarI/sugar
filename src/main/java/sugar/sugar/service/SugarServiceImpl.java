package sugar.sugar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sugar.sugar.dto.NewSugar;
import sugar.sugar.dto.SugarDto;
import sugar.sugar.dto.UpdateSugar;
import sugar.sugar.exception.ValidationException;
import sugar.sugar.interfaces.SugarService;
import sugar.sugar.maper.SugarMapper;
import sugar.sugar.model.Sugar;
import sugar.sugar.repository.SugarRepository;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SugarServiceImpl implements SugarService {
    private final SugarRepository sugarRepository;
    @Override
    public SugarDto addEntry(NewSugar newSugar) {
        String info;
        log.info("Новая запись:\n{}", newSugar);

        if (newSugar == null) {
            info = "Недостаточно данных для добавления новой записи";
            log.debug(info);
            throw new ValidationException(info);
        }

        Sugar sugar = Sugar.builder()
                .levelSugar(newSugar.getSugarLevel())
                .doseOfInsulin(newSugar.getDoseOfInsulin())
                .time(LocalDateTime.now())
                .note(newSugar.getNote())
                .build();

        Sugar sugarSave = sugarRepository.save(sugar);

        Sugar sugarStory = sugarRepository.findByLevelSugar(sugarSave.getLevelSugar());

        SugarDto sugarDto = SugarMapper.toDto(sugarSave, sugarStory.getId(), sugarStory.getDoseOfInsulin(), sugarStory.getTime());

        log.info("Вернули объект: {}", sugarDto);

        return sugarDto;
    }

    @Override
    public Sugar updateEntry(UpdateSugar updateSugar) {
        if (updateSugar == null) {
            throw new ValidationException("Не достаточно данных для обновления");
        }
        log.info("Полученные данные для обновления: " + updateSugar);
    }

    @Override
    @Transactional(readOnly = true)
    public SugarDto getSugarById(Long sugarId) {
        return null;
    }

    @Override
    public void removeSugarById(Long sugarId) {
        sugarRepository.deleteById(sugarId);
    }

    @Override
    public void clearAll() {
        sugarRepository.deleteAll();
    }
}
