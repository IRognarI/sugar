package sugar_bot.sugar.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
@Setter
public class SugarDto {
    @Builder.Default
    private Long id = 0L;

    @Builder.Default
    private double lastDoseOfInsulin = 0;

    @Builder.Default
    private String lastDate = "date not found";

    private Long sugarId;

    @NotNull(message = "Уровень сахара должен быть обязательно указан")
    private double levelSugar;
    private double doseOfInsulin;
    private String time;
    private String note;
}
