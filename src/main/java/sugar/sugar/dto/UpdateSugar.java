package sugar.sugar.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class UpdateSugar {
    @Positive(message = "ID записи должен быть положительным")
    private Long sugarId;

    @Length(max = 255, message = "Максимальная длина записи должна быть не более 255 символов")
    private String note;
    private double doseOfInsulin;
    private double sugarLevel;
}
