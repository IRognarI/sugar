package sugar.sugar.dto;

import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@Builder
public class NewSugar {
    @Positive(message = "Уровень сахара должен быть больше ноля")
    private double sugarLevel;

    @Builder.Default
    private double doseOfInsulin = 0.0;
    @Length(max = 255, message = "Максимальное кол-во символов: 255")
    private String note;
}
