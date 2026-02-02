package sugar_bot.sugar.dto;

import jakarta.validation.constraints.NotNull;
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
@Getter
@Setter
@Builder
@ToString
public class NewSugar {
    @Positive(message = "Уровень сахара должен быть больше ноля")
    private double sugarLevel;

    @Positive(message = "chatId должен быть больше ноля")
    @NotNull(message = "chatId должен быть указан")
    private long chatId;

    @Builder.Default
    private double doseOfInsulin = 0;
    @Length(max = 255, message = "Максимальное кол-во символов: 255")
    @Builder.Default
    private String note = "With out notes";
}
