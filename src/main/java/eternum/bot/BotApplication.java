package eternum.bot;

import eternum.bot.service.BotExchangeService;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotApplication {
    public static void main(String[] args) {
        try {
            SimpleTelegramBot bot = new SimpleTelegramBot();
            BotExchangeService.init(bot);
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(
                    BotExchangeService::updateRatesAndCheckAlerts,
                    0, 15, TimeUnit.MINUTES
            );

            System.out.println("Запуск Telegram бота...");
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            System.out.println("Бот успешно запущен!");

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}