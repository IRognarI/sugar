package sugar.telegram.state;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sugar.sugar.dto.NewSugar;
import sugar.sugar.dto.UpdateSugar;
import sugar.telegram.enums.State;

@NoArgsConstructor
@Getter
@Setter
public class UserSt {
    private State state;
    private NewSugar newSugar;
    private UpdateSugar update;
}
