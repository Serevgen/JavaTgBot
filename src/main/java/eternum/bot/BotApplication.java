package eternum.bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Класс для запуска приложения и регистрации бота
 */
public class BotApplication {
    public static void main(String[] args) {
        try {
            System.out.println("Запуск Telegram бота...");

            // Создае System.out.prin API для работы с ботами
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // Регистрируем нашего бота
            botsApi.registerBot(new SimpleTelegramBot());

            System.out.println("Бот успешно запущен и готов к работе!");
            System.out.println("Для остановки нажмите Ctrl+C");

        } catch (TelegramApiException e) {
            System.err.println("Ошибка при запуске бота: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
