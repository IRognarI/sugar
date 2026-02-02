package sugar_bot.telegram.menu;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import sugar_bot.telegram.util.message.Message;

import java.util.LinkedList;
import java.util.List;

@UtilityClass
public class Menu {

    public static void sendMenu(Long chatId, Message message) {
        checkMenu(chatId, message);
    }

    private void checkMenu(Long chatId, Message message) {

        if (chatId == Long.parseLong(System.getenv("my_chat_token"))) {

            SendMessage sendMessage = message.sendMessage(chatId, "Вот что я могу:");

            InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                    .text("Создать запись")
                    .callbackData("addEntry")
                    .build();

            InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                    .text("Обновить запись")
                    .callbackData("updateEntry")
                    .build();

            InlineKeyboardButton button3 = InlineKeyboardButton.builder()
                    .text("Получить запись по ID")
                    .callbackData("getById")
                    .build();

            InlineKeyboardButton button4 = InlineKeyboardButton.builder()
                    .text("Создать напоминание")
                    .callbackData("createNotify")
                    .build();

            InlineKeyboardButton button5 = InlineKeyboardButton.builder()
                    .text("Отключить напоминания")
                    .callbackData("disableNotify")
                    .build();

            InlineKeyboardButton button6 = InlineKeyboardButton.builder()
                    .text("Удалить запись")
                    .callbackData("removeById")
                    .build();

            InlineKeyboardButton button7 = InlineKeyboardButton.builder()
                    .text("Получить записи за период")
                    .callbackData("getEntryForPeriod")
                    .build();

            List<InlineKeyboardRow> keyboards = List.of(
                    new InlineKeyboardRow(button1),
                    new InlineKeyboardRow(button2),
                    new InlineKeyboardRow(button3),
                    new InlineKeyboardRow(button4),
                    new InlineKeyboardRow(button5),
                    new InlineKeyboardRow(button6),
                    new InlineKeyboardRow(button7)
            );

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(keyboards);

            sendMessage.setReplyMarkup(keyboardMarkup);

            message.execute(sendMessage);

        } else {

            SendMessage sendMessage = message.sendMessage(chatId, "Вот что я могу:");

            InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                    .text("Создать запись")
                    .callbackData("addEntry")
                    .build();

            InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                    .text("Обновить запись")
                    .callbackData("updateEntry")
                    .build();

            InlineKeyboardButton button3 = InlineKeyboardButton.builder()
                    .text("Получить запись по ID")
                    .callbackData("getById")
                    .build();

            InlineKeyboardButton button4 = InlineKeyboardButton.builder()
                    .text("Создать напоминание")
                    .callbackData("createNotify")
                    .build();

            InlineKeyboardButton button5 = InlineKeyboardButton.builder()
                    .text("Отключить напоминания")
                    .callbackData("disableNotify")
                    .build();

            InlineKeyboardButton button6 = InlineKeyboardButton.builder()
                    .text("Получить записи за период")
                    .callbackData("getEntryForPeriod")
                    .build();

            List<InlineKeyboardRow> keyboards = new LinkedList<>(List.of(
                    new InlineKeyboardRow(button1),
                    new InlineKeyboardRow(button2),
                    new InlineKeyboardRow(button3),
                    new InlineKeyboardRow(button4),
                    new InlineKeyboardRow(button5),
                    new InlineKeyboardRow(button6)
            ));

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(keyboards);

            sendMessage.setReplyMarkup(keyboardMarkup);

            message.execute(sendMessage);
        }
    }
}
