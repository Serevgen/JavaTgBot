package eternum.bot.service;

import eternum.bot.model.Currency;
import java.util.List;

public class AllValutes {

    public static String rates() {
        StringBuilder sb = new StringBuilder("Доступные базовые курсы:\n\n");

        sb.append("Фиатные валюты (к Рублю):\n");
        try {
            List<Currency> cbrCurrencies = CbrApiClient.listOfValutes();
            for (Currency currency : cbrCurrencies) {
                sb.append(currency.getName())
                        .append(" (").append(currency.getCharCode()).append(") - ")
                        .append(String.format("%.4f ₽", currency.getUnitRate())).append("\n");
            }
        } catch (Exception e) {
            sb.append(" Не удалось загрузить курсы ЦБ РФ.\n");
        }

        sb.append("\nКриптовалюты (к Доллару):\n");
        try {
            List<Currency> cryptoCurrencies = CryptoApiClient.listOfCrypto();
            if (cryptoCurrencies.isEmpty()) {
                sb.append("Список криптовалют пуст.\n");
            } else {
                for (Currency currency : cryptoCurrencies) {
                    sb.append(currency.getName())
                            .append(" - ")
                            .append(String.format("%.2f $", currency.getUnitRate())).append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("Не удалось загрузить курсы криптовалют.\n");
        }

        return sb.toString();
    }
}