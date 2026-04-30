package eternum.bot.service;

import com.google.gson.Gson;
import eternum.bot.model.Currency;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CbrApiClient {

    public static List<Currency> listOfValutes() {
        try {
            String json;
            try (InputStream in = new URL("https://www.cbr-xml-daily.ru/daily_json.js").openStream()) {
                json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }

            JSONObject currenciesData = new JSONObject(json).getJSONObject("Valute");
            Gson gson = new Gson();

            List<Currency> currencies = currenciesData.keySet()
                    .stream()
                    .map((currencyKey) -> gson.fromJson(
                            currenciesData.getJSONObject(currencyKey).toString(),
                            Currency.class
                    ))
                    .toList();

            return currencies;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке данных о валютах от ЦБ РФ", e);
        }
    }
}