package sugar_bot.zoneId;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.time.ZoneId;

@UtilityClass
public class TargetZoneId {

    @Getter
    private static final ZoneId zoneId = ZoneId.of("Europe/Moscow");
}
