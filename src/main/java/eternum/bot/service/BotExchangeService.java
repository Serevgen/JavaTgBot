package eternum.bot.service;

import eternum.bot.SimpleTelegramBot;
import eternum.bot.model.Currency;

import java.util.List;

public class BotExchangeService {

    private static final CurrencyGraphManager graph = new CurrencyGraphManager();
    private static AlertManager alertManager;

    public static void init(SimpleTelegramBot bot) {
        alertManager = new AlertManager(bot);
    }

    public static double getCurrentRate(String pair) {
        pair = pair.toUpperCase().replace(" ", "");

        // 1. Проверяем, есть ли готовый курс в графе (базовый или уже вычисленный кросс-курс)
        double rate = graph.getCalculatedRate(pair);
        if (rate != -1.0) return rate;

        // 2. Если это кросс-курс без RUB, пробуем рассчитать динамически
        if (pair.contains("/")) {
            String[] parts = pair.split("/");
            if (parts.length == 2) {
                return calculateDynamicCrossRate(parts[0], parts[1]);
            }
        } else if (pair.length() == 6) {
            return calculateDynamicCrossRate(pair.substring(0, 3), pair.substring(3));
        }

        return -1.0;
    }

    private static double calculateDynamicCrossRate(String base, String target) {
        double baseToRub = graph.getCalculatedRate(base + "/RUB");
        double targetToRub = graph.getCalculatedRate(target + "/RUB");

        if (baseToRub > 0 && targetToRub > 0) {
            return baseToRub / targetToRub;
        }
        return -1.0;
    }

    public static void updateRatesAndCheckAlerts() {
        try {
            System.out.println("Обновление курсов и проверка уведомлений...");

            List<Currency> cbrData = CbrApiClient.listOfValutes();

            graph.initBaseRatesFromCbr(cbrData);
            graph.addCrossRateTarget("USD/EUR", "USD/RUB", "EUR/RUB");
            graph.calculateWithTopologicalSort();

            if (alertManager != null) {
                alertManager.checkAlertsAndNotify(graph); //
            }

            System.out.println("Обновление успешно завершено.");
        } catch (Exception e) {
            System.err.println("Ошибка в цикле обновления: " + e.getMessage());
        }
    }

}
