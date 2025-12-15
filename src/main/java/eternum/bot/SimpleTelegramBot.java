package eternum.bot;

import eternum.bot.commands.CommandHandler;
import eternum.bot.config.BotConfig;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Основной класс Telegram бота
 */
public class SimpleTelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final CommandHandler commandHandler;

    public SimpleTelegramBot() {
        this.config = new BotConfig();
        this.commandHandler = new CommandHandler();
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, есть ли новое сообщение
        if (update.hasMessage() && update.getMessage().hasText()) {
            var message = update.getMessage();
            var chatId = message.getChatId();

            try {
                // Обрабатываем команду и отправляем ответ
                SendMessage response = commandHandler.handleCommand(message);
                execute(response);
            } catch (TelegramApiException e) {
                System.err.println("Ошибка при отправке сообщения: " + e.getMessage());

                // Пытаемся отправить сообщение об ошибке
                try {
                    SendMessage errorMessage = new SendMessage();
                    errorMessage.setChatId(chatId.toString());
                    errorMessage.setText("Произошла ошибка при обработке запроса.");
                    execute(errorMessage);
                } catch (TelegramApiException ex) {
                    System.err.println("Не удалось отправить сообщение об ошибке: " + ex.getMessage());
                }
            }
        }
    }
}