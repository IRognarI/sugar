package sugar_bot.telegram.util.admin;

import lombok.Getter;

public class Admin {
    @Getter
    private static final String admin = System.getenv("admin");

    /*Добавить в базу колонку shatId для идентификации принадлежности записи пользователя
     * Реализовать статистику (вывод записей за определенный период)
     * */
}
