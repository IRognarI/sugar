package sugar.telegram.menu;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import sugar.telegram.util.message.Message;

import java.util.List;

@UtilityClass
public class Menu {

    public static void sendMenu(Long chatId, Message message) {
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

        /*InlineKeyboardButton button4 = InlineKeyboardButton.builder()
                .text("Удалить запись")
                .callbackData("removeById")
                .build();*/

        List<InlineKeyboardRow> keyboards = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3)
                //new InlineKeyboardRow(button4)
        );

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(keyboards);

        sendMessage.setReplyMarkup(keyboardMarkup);

        message.execute(sendMessage);
    }
}
