package sugar_bot.telegram.util.admin;

import lombok.Getter;

public class Admin {
    @Getter
    private static final String admin = System.getenv("admin");
}
