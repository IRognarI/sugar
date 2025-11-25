package sugar.sugar.service;

import jakarta.validation.Valid;
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
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SugarServiceImpl implements SugarService {
    private final SugarRepository sugarRepository;

    @Override
    public SugarDto addEntry(@Valid NewSugar newSugar) throws ValidationException {
        String info;
        log.info("Новая запись: {}", newSugar);

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

        Optional<Sugar> sugarStory = findDateTimeMax(sugarRepository.findByLevelSugar(sugarSave.getLevelSugar(), sugarSave.getId()));
        SugarDto sugarDto = SugarMapper.toDto(sugarSave, sugarStory);

        log.info("Вернули объект: {}", sugarDto);

        return sugarDto;
    }

    @Override
    public Sugar updateEntry(UpdateSugar updateSugar) {
        if (updateSugar == null) {
            throw new ValidationException("Не достаточно данных для обновления");
        }
        log.info("Полученные данные для обновления: " + updateSugar);

        return null;
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

    private Optional<Sugar> findDateTimeMax(List<Sugar> sugarList) {
        if (sugarList.isEmpty()) {
            return Optional.empty();
        }

        if (sugarList.size() == 1) {
            return Optional.of(sugarList.getFirst());
        }

        Optional<Sugar> dateTimeMax = Optional.of(sugarList.getFirst());

        for (int i = 1; i < sugarList.size(); i++) {

            if (sugarList.get(i).getTime().isAfter(dateTimeMax.get().getTime())) {
                dateTimeMax = Optional.of(sugarList.get(i));
            }
        }
        return dateTimeMax;
    }
}
