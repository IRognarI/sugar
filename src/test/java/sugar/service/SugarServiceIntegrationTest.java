package sugar.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sugar.sugar.dto.NewSugar;
import sugar.sugar.dto.SugarDto;
import sugar.sugar.dto.UpdateSugar;
import sugar.sugar.exception.ValidationException;
import sugar.sugar.interfaces.SugarService;
import sugar.sugar.model.Sugar;
import sugar.sugar.repository.SugarRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class SugarServiceIntegrationTest {

    @Autowired
    private SugarService sugarService;

    @Autowired
    private SugarRepository sugarRepository;

    private NewSugar newSugar1;
    private NewSugar newSugar2;
    private NewSugar newSugar3;

    private double levelSugar = 7.1;
    private long chatId = 4234234;
    LocalDateTime date = LocalDateTime.of(2025, 11, 19, 12, 0, 0);

    Sugar sugar1;
    Sugar sugar2;
    Sugar sugar3;
    Sugar sugar4;

    List<Sugar> sugarList = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        newSugar1 = NewSugar.builder()
                .build();

        newSugar2 = NewSugar.builder().sugarLevel(7.0).note("s".repeat(300)).build();

        newSugar3 = NewSugar.builder()
                .sugarLevel(levelSugar)
                .doseOfInsulin(0.25)
                .note("Можно не колоть")
                .build();

        sugar1 = Sugar.builder()
                .id(1L)
                .chatId(chatId)
                .levelSugar(newSugar3.getSugarLevel())
                .doseOfInsulin(newSugar3.getDoseOfInsulin())
                .time(date)
                .note(newSugar3.getNote())
                .build();

        sugar2 = Sugar.builder()
                .id(sugar1.getId() + 1)
                .chatId(chatId + 1)
                .levelSugar(newSugar3.getSugarLevel())
                .doseOfInsulin(sugar1.getDoseOfInsulin())
                .time(sugar1.getTime().plusDays(2))
                .note(sugar1.getNote())
                .build();

        sugar3 = Sugar.builder()
                .id(sugar2.getId() + 1)
                .chatId(chatId + 2)
                .levelSugar(newSugar3.getSugarLevel())
                .doseOfInsulin(sugar1.getDoseOfInsulin())
                .time(sugar1.getTime().minusDays(1))
                .note(sugar1.getNote())
                .build();

        sugar4 = Sugar.builder()
                .id(sugar3.getId() + 1)
                .chatId(chatId + 3)
                .levelSugar(newSugar3.getSugarLevel())
                .doseOfInsulin(sugar1.getDoseOfInsulin())
                .time(LocalDateTime.now().minusHours(1))
                .note(sugar1.getNote())
                .build();

        sugarList.add(sugar1);
        sugarList.add(sugar2);
        sugarList.add(sugar3);
        sugarList.add(sugar4);
    }

    @Test
    public void addEntry_shouldBeConstraintViolationException_whenSugarLevelIsZero() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<NewSugar>> violations = validator.validate(newSugar1);

        Assertions.assertFalse(violations.isEmpty());
        violations.stream()
                .map(ConstraintViolation::getMessage)
                .forEach(System.out::println);
    }

    @Test
    public void addEntry_shouldBeConstraintViolationException_whenCountCharInNoteIsMoreThan255() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<NewSugar>> violations = validator.validate(newSugar2);

        Assertions.assertFalse(violations.isEmpty());
        violations.stream().map(ConstraintViolation::getMessage).forEach(System.out::println);
    }

    @Test
    public void addEntry_shouldBeCorrect() {
        sugarRepository.save(sugar1.toBuilder().id(null).build());
        sugarRepository.save(sugar2.toBuilder().id(null).build());
        sugarRepository.save(sugar3.toBuilder().id(null).build());
        sugarRepository.save(sugar4.toBuilder().id(null).build());

        SugarDto sugarDto = sugarService.addEntry(newSugar3);

        Assertions.assertNotNull(sugarDto, "SugarDto is Null");
    }

    @Test
    public void updateEntry_shouldBeCorrect() {
        Sugar sugar = sugarRepository.save(sugar1.toBuilder().id(null).build());

        UpdateSugar updateSugar = new UpdateSugar(sugar.getId(), "Заметка для новой записи", 1.5, 12.1);

        SugarDto newSugar = sugarService.updateEntry(updateSugar);

        Assertions.assertEquals(newSugar.getSugarId(), sugar.getId());
        Assertions.assertEquals(updateSugar.getNote(), newSugar.getNote());
        Assertions.assertEquals(newSugar.getDoseOfInsulin(), updateSugar.getDoseOfInsulin());
        Assertions.assertEquals(newSugar.getLevelSugar(), updateSugar.getSugarLevel());
    }

    @Test
    public void updateSugar_shouldBeValidationException_WhenUpdateSugarIsNull() {
        UpdateSugar updateSugar = null;

        ValidationException thrown = Assertions.assertThrows(ValidationException.class,
                () -> sugarService.updateEntry(updateSugar));

        System.out.println(thrown.getMessage());
    }

    @Test
    public void updateSugar_shouldBeConstraintViolationException_WhenSugarIdIsLessOrEqualToZero() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        UpdateSugar updateSugar = new UpdateSugar(0L, "Заметка для новой записи", 1.5, 12.1);

        Set<ConstraintViolation<UpdateSugar>> violations = validator.validate(updateSugar);

        Assertions.assertFalse(violations.isEmpty());

        violations.stream().map(ConstraintViolation::getMessage).forEach(System.out::println);
    }

    @Test
    public void updateSugar_shouldBeConstraintViolationException_WhenSugarNoteIsMoreThan255Characters() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Sugar sugar = sugarRepository.save(sugar1.toBuilder().id(null).build());

        UpdateSugar updateSugar = new UpdateSugar(sugar.getId(), "Заметка для новой записи".repeat(220), 1.5, 12.1);

        Set<ConstraintViolation<UpdateSugar>> violations = validator.validate(updateSugar);

        Assertions.assertFalse(violations.isEmpty());

        violations.stream().map(ConstraintViolation::getMessage).forEach(System.out::println);
    }

    @Test
    public void updateSugar_partialUpdate_shouldBeCorrect() {
        Sugar target = sugarRepository.save(sugar1.toBuilder().id(null).build());

        UpdateSugar updateSugar = UpdateSugar.builder().sugarId(target.getId()).doseOfInsulin(1.5).build();

        SugarDto update = sugarService.updateEntry(updateSugar);

        Assertions.assertNotNull(update);
        Assertions.assertEquals(target.getId(), update.getSugarId());
        Assertions.assertTrue(update.getDoseOfInsulin() > 0);
    }
}
