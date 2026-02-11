package sugar_bot.telegram.userCheck;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sugar_bot.sugar.dto.NewSugar;
import sugar_bot.sugar.dto.UpdateSugar;
import sugar_bot.telegram.enums.State;

@NoArgsConstructor
@Getter
@Setter
public class UserCheck {
    private State state;
    private boolean getNotify = false;
    private NewSugar newSugar;
    private UpdateSugar update;
}
