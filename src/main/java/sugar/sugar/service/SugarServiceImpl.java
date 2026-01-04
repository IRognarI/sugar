package sugar.sugar.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sugar.sugar.dto.NewSugar;
import sugar.sugar.dto.SugarDto;
import sugar.sugar.dto.UpdateSugar;
import sugar.sugar.exception.DateErrorException;
import sugar.sugar.exception.NotFoundException;
import sugar.sugar.exception.ValidationException;
import sugar.sugar.interfaces.SugarService;
import sugar.sugar.maper.SugarMapper;
import sugar.sugar.model.Sugar;
import sugar.sugar.repository.SugarRepository;
import sugar.sugar.dateTimeFormater.DateTimeFormat;

import java.time.LocalDate;
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

        Optional<Sugar> sugarStory = getSugarStory(sugarSave);
        SugarDto sugarDto = SugarMapper.toDto(sugarSave, sugarStory);

        if (!sugarDto.getLastDate().equalsIgnoreCase("date not found") &&
                checkDate(DateTimeFormat.dateFromString(sugarDto.getLastDate()),
                        DateTimeFormat.getDateTimeFormatter(sugarDto.getTime()))) {
            throw new DateErrorException("Дата прошлой записи позже текущей");
        }

        log.info("Вернули объект: {}", sugarDto);

        return sugarDto;
    }

    @Override
    public SugarDto updateEntry(@Valid UpdateSugar updateSugar) throws ValidationException, NotFoundException {
        if (updateSugar == null) {
            throw new ValidationException("Не достаточно данных для обновления");
        }
        log.info("Полученные данные для обновления: " + updateSugar);

        Sugar sugarOld = sugarRepository.getSugarById(updateSugar.getSugarId())
                .orElseThrow(() -> new NotFoundException("Запись с ID " + updateSugar.getSugarId() + " - не найдена"));

        if (updateSugar.getNote() != null) {
            sugarOld.setNote(updateSugar.getNote());
        }

        if (updateSugar.getSugarLevel() != 0) {
            sugarOld.setLevelSugar(updateSugar.getSugarLevel());
        }

        if (updateSugar.getDoseOfInsulin() != 0) {
            sugarOld.setDoseOfInsulin(updateSugar.getDoseOfInsulin());
        }

        Sugar newSugar = sugarRepository.save(sugarOld);

        Optional<Sugar> sugarStory = getSugarStory(newSugar);

        SugarDto sugarDto = SugarMapper.toDto(newSugar, sugarStory);

        if (!sugarDto.getLastDate().equalsIgnoreCase("date not found") &&
                checkDate(DateTimeFormat.dateFromString(sugarDto.getLastDate()),
                        DateTimeFormat.getDateTimeFormatter(sugarDto.getTime()))) {
            throw new DateErrorException("Дата прошлой записи позже текущей");
        }

        log.info("Обновленная запись {}", sugarDto);

        return sugarDto;
    }

    @Override
    @Transactional(readOnly = true)
    public SugarDto getSugarById(Long sugarId) throws ValidationException, NotFoundException {

        if (sugarId == null || sugarId < 1) {
            throw new ValidationException("ID записи не может быть " + sugarId);
        }

        log.info("Получили ID для поиска записи: {}", sugarId);

        Sugar sugar = sugarRepository.getSugarById(sugarId)
                .orElseThrow(() -> new NotFoundException("Запись с ID=" + sugarId + " - не найдена"));

        Optional<Sugar> sugarStory = getSugarStory(sugar);

        SugarDto sugarDto = SugarMapper.toDto(sugar, sugarStory);

        if (!sugarDto.getLastDate().equalsIgnoreCase("date not found") &&
                checkDate(DateTimeFormat.dateFromString(sugarDto.getLastDate()),
                        DateTimeFormat.getDateTimeFormatter(sugarDto.getTime()))) {
            throw new DateErrorException("Дата прошлой записи позже текущей");
        }

        log.info("Вернули запись: {}", sugarDto);

        return sugarDto;
    }

    @Override
    public void removeSugarById(Long sugarId) {
        if (sugarId != null && sugarId > 0) {
            sugarRepository.deleteById(sugarId);
        }
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

    private Optional<Sugar> getSugarStory(Sugar sugarSave) {
        return findDateTimeMax(sugarRepository.findByLevelSugar(sugarSave.getLevelSugar(), sugarSave.getId()));
    }

    private boolean checkDate(LocalDate last, LocalDate actual) {
        return last.isAfter(actual);
    }
}
