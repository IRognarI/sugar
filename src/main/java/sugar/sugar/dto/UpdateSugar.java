package sugar.sugar.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@ToString
@Getter
public class UpdateSugar {
    @Positive(message = "ID записи должен быть положительным")
    private Long sugarId;

    @Length(max = 255, message = "Максимальная длина записи должна быть не более 255 символов")
    private String note;

    @Builder.Default
    private double doseOfInsulin = 0;

    @Builder.Default
    private double sugarLevel = 0;
}
