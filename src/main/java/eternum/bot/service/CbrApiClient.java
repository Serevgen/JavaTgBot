package eternum.bot.service;

import com.google.gson.Gson;
import eternum.bot.model.Currency;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;



public class CbrApiClient {
    private static String valutes;


    public static List<Currency> listOfValutes() {

        try {
            String json = new String(new URL("https://www.cbr-xml-daily.ru/daily_json.js").openStream().readAllBytes());

            System.out.println(json);

            JSONObject currenciesData = new JSONObject(json).getJSONObject("Valute");

            List<Currency> currencies = currenciesData.keySet()
                    // Преобразовывает Set в Stream.
                    .stream()
                    // При помощи GSON переводит JSON строчку валюты к классу Currency.
                    .map((currency) ->
                            new Gson().fromJson(
                                    currenciesData.getJSONObject(currency)
                                            .toString(),
                                    Currency.class
                            )
                    )
                    // Преобразовывает Stream в List.
                    .toList();

            // Выводит информацию в консоль.
            //currencies.forEach((currency) ->
                    //System.out.println(currency.getName() + " (" + currency.getChCode() + ") - " + currency.getNominal())
                    //valutes += currency.getName() + " (" + currency.getCharCode() + ") - " + currency.getValue() + "\n"
            //);
            //valutes = valutes.substring(4);// + "\n" + currencies.get(56);
            return currencies;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}

