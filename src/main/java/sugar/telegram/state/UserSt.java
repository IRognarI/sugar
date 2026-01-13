package sugar.telegram.state;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sugar.sugar.dto.NewSugar;
import sugar.sugar.dto.UpdateSugar;
import sugar.telegram.enums.State;

import java.util.concurrent.atomic.AtomicBoolean;

@NoArgsConstructor
@Getter
@Setter
public class UserSt {
    private State state;
    private AtomicBoolean getNotify = new AtomicBoolean(true);
    private NewSugar newSugar;
    private UpdateSugar update;
}
