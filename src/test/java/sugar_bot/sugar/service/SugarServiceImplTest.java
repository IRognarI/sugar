package sugar_bot.sugar.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sugar_bot.sugar.dto.NewSugar;
import sugar_bot.sugar.dto.SugarDto;
import sugar_bot.sugar.exception.NotFoundException;
import sugar_bot.sugar.exception.ValidationException;
import sugar_bot.sugar.model.Sugar;
import sugar_bot.sugar.repository.SugarRepository;
import sugar_bot.sugar.service.SugarServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class SugarServiceImplTest {

    @Mock
    private SugarRepository sugarRepository;

    @InjectMocks
    private SugarServiceImpl sugarService;

    NewSugar newSugar1;
    NewSugar newSugar2;
    NewSugar newSugar3;
    NewSugar newSugar4;
    Sugar sugarNotFull1;
    Sugar sugarNotFull2;
    Sugar sugarFull1;
    Sugar sugarFull2;
    List<Sugar> sugarList = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        newSugar1 = new NewSugar(7, 435, 0.25, null); // Новая запись без заметки
        newSugar2 = new NewSugar(4, 34534, 0.0, null); // Новая запись без инсулина и заметки
        newSugar3 = new NewSugar(0, 34534, 0.75, "При таком сахаре можно не колоть"); // Новая запись с 0 сахаром и заметкой

        sugarNotFull1 = Sugar.builder()
                .levelSugar(newSugar1.getSugarLevel())
                .doseOfInsulin(newSugar1.getDoseOfInsulin())
                .time(LocalDateTime.of(2025, 11, 21, 13, 0, 0))
                .note(newSugar1.getNote())
                .build();

        sugarFull1 = sugarNotFull1.toBuilder().id(1L).build();

        sugarNotFull2 = Sugar.builder()
                .levelSugar(newSugar2.getSugarLevel())
                .doseOfInsulin(newSugar2.getDoseOfInsulin())
                .time(LocalDateTime.of(2025, 11, 25, 11, 00, 0))
                .note(newSugar2.getNote())
                .build();

        sugarFull2 = sugarNotFull2.toBuilder().id(3L).build();

        sugarList.add(sugarFull1);
        sugarList.add(sugarFull2);
    }

    @Test
    public void addEntry_shouldBeCorrect() {
        Mockito.when(sugarRepository.save(Mockito.any(Sugar.class))).thenReturn(sugarFull1);
        Mockito.when(sugarRepository.findByLevelSugar(Mockito.anyDouble(), Mockito.anyLong())).thenReturn(List.of(sugarFull1));

        SugarDto sugarDto = sugarService.addEntry(newSugar1);

        Assertions.assertNotNull(sugarDto);
        Assertions.assertEquals(1L, sugarDto.getSugarId());
        Assertions.assertEquals(newSugar1.getSugarLevel(), sugarDto.getLevelSugar());
        Assertions.assertEquals(newSugar1.getDoseOfInsulin(), sugarDto.getDoseOfInsulin());
        Assertions.assertEquals("21.11.2025 13:00", sugarDto.getTime());
        Assertions.assertEquals(sugarFull1.getId(), sugarDto.getId());
        Assertions.assertEquals(sugarFull1.getDoseOfInsulin(), sugarDto.getLastDoseOfInsulin());
        Assertions.assertNull(sugarDto.getNote());
    }

    @Test
    public void addEntry_shouldBeValidationException_WhenNewSugarIsNull() {
        ValidationException thrown = Assertions.assertThrows(ValidationException.class, () -> sugarService.addEntry(newSugar4));
        System.out.println(thrown.getMessage());
    }

    @Test
    public void addEntry_shouldBeSugarDtoIsDefault() {
        Mockito.when(sugarRepository.save(Mockito.any(Sugar.class))).thenReturn(sugarFull1);
        Mockito.when(sugarRepository.findByLevelSugar(Mockito.anyDouble(), Mockito.anyLong())).thenReturn(List.of());

        SugarDto sugarDto = sugarService.addEntry(newSugar2);

        Assertions.assertNotNull(sugarDto, "SugarDto is null");
        Assertions.assertEquals(0, sugarDto.getId());
        Assertions.assertEquals(0, sugarDto.getLastDoseOfInsulin());
        Assertions.assertEquals("date not found", sugarDto.getLastDate());
    }

    @Test
    public void getSugarById_shouldBeCorrect() {
        Mockito.when(sugarRepository.getSugarById(Mockito.anyLong())).thenReturn(Optional.of(sugarFull1));
        Mockito.when(sugarRepository.findByLevelSugar(Mockito.anyDouble(), Mockito.anyLong())).thenReturn(List.of());

        SugarDto sugarDto = sugarService.getSugarById(sugarFull1.getId());

        Assertions.assertNotNull(sugarDto);
        Assertions.assertEquals(0, sugarDto.getId());
        Assertions.assertEquals(0, sugarDto.getLastDoseOfInsulin());
        Assertions.assertEquals("date not found", sugarDto.getLastDate());
    }

    @Test
    public void getSugarById_shouldBeValidationException_WhenSugarIdIsNotValid() {
        ValidationException sugarIdIsZero = Assertions.assertThrows(ValidationException.class,
                () -> sugarService.getSugarById(0L));
        System.out.println(sugarIdIsZero.getMessage());

        ValidationException sugarIdIsNull = Assertions.assertThrows(ValidationException.class,
                () -> sugarService.getSugarById(null));
        System.out.println(sugarIdIsNull.getMessage());
    }

    @Test
    public void getSugarId_shouldBeNotFoundException_WhenSugarNotFoundById() {
        NotFoundException thrown = Assertions.assertThrows(NotFoundException.class,
                () -> sugarService.getSugarById(4L));
        System.out.println(thrown.getMessage());
    }
}
