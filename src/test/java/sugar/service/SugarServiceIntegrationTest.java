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
import org.springframework.test.context.ActiveProfiles;
import sugar.sugar.dto.NewSugar;
import sugar.sugar.dto.SugarDto;
import sugar.sugar.interfaces.SugarService;
import sugar.sugar.model.Sugar;
import sugar.sugar.repository.SugarRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest
@ActiveProfiles("test")
public class SugarServiceIntegrationTest {

    @Autowired
    private SugarService sugarService;

    @Autowired
    private SugarRepository sugarRepository;

    NewSugar newSugar1;
    NewSugar newSugar2;
    NewSugar newSugar3;

    double levelSugar = 7.1;
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
                .levelSugar(newSugar3.getSugarLevel())
                .doseOfInsulin(newSugar3.getDoseOfInsulin())
                .time(date)
                .note(newSugar3.getNote())
                .build();

        sugar2 = Sugar.builder()
                .id(sugar1.getId() + 1)
                .levelSugar(newSugar3.getSugarLevel())
                .doseOfInsulin(sugar1.getDoseOfInsulin())
                .time(sugar1.getTime().plusDays(2))
                .note(sugar1.getNote())
                .build();

        sugar3 = Sugar.builder()
                .id(sugar2.getId() + 1)
                .levelSugar(newSugar3.getSugarLevel())
                .doseOfInsulin(sugar1.getDoseOfInsulin())
                .time(sugar1.getTime().minusDays(1))
                .note(sugar1.getNote())
                .build();

        sugar4 = Sugar.builder()
                .id(sugar3.getId() + 1)
                .levelSugar(newSugar3.getSugarLevel())
                .doseOfInsulin(sugar1.getDoseOfInsulin())
                .time(sugar3.getTime().plusWeeks(3))
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
        Assertions.assertEquals(sugar4.getId(), sugarDto.getId());
    }
}
