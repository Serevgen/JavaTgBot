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

        double rate = graph.getCalculatedRate(pair);
        if (rate != -1.0) return rate;

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
            System.out.println("Обновление курсов...");

            graph.clear();

            List<Currency> cbrData = CbrApiClient.listOfValutes();
            List<Currency> cryptoData = CryptoApiClient.listOfCrypto();

            graph.initBaseRatesFromCbr(cbrData);
            graph.initBaseRatesFromCrypto(cryptoData);


            graph.addCrossRateTarget("USD/EUR", "USD/RUB", "EUR/RUB", false);

            graph.addCrossRateTarget("BTC/RUB", "BTC/USD", "USD/RUB", true);
            graph.addCrossRateTarget("ETH/RUB", "ETH/USD", "USD/RUB", true);
            graph.addCrossRateTarget("BNB/RUB", "BNB/USD", "USD/RUB", true);


            graph.addCrossRateTarget("BTC/EUR", "BTC/RUB", "EUR/RUB", false);
            graph.addCrossRateTarget("ETH/EUR", "ETH/RUB", "EUR/RUB", false);

            // Запускаем вычисления
            graph.calculateWithTopologicalSort();

            if (alertManager != null) {
                alertManager.checkAlertsAndNotify(graph);
            }

            System.out.println("Обновление успешно завершено.");
        } catch (Exception e) {
            System.err.println("Ошибка в цикле обновления: " + e.getMessage());
            e.printStackTrace();
        }
    }
}