package sugar.sugar.dto;

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
    private double lastDoseOfInsulin = 0.0;
    private String lastDate;
    private Long sugarId;
    private double levelSugar;
    private double doseOfInsulin;
    private String time;
    private String note;
}
