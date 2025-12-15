package eternum.bot.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.junit.jupiter.api.Assertions.*;

class CommandHandlerTest {

    @org.junit.jupiter.api.Test
    void handleCommand() {
        String expected = "Добро пожаловать!\n\n" +
                "Это простой телеграм бот, созданный на Java.\n" +
                "Используйте /help для просмотра доступных команд.";

        Message message = new Message();

        Chat chat = new Chat();
        chat.setId(1L);

        message.setText("/start");
        message.setChat(chat);

        CommandHandler commandHandler = new CommandHandler();

        String actual = commandHandler.handleCommand(message).getText();
        assertEquals(expected, actual);
    }
}