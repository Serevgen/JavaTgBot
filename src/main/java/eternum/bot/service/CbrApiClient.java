package eternum.bot.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eternum.bot.model.Currency;

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

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject valuteMap = root.getAsJsonObject("Valute");

            return valuteMap.keySet().stream()
                    .map(key -> Currency.fromCbrJson(valuteMap.getAsJsonObject(key)))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке данных о валютах от ЦБ РФ", e);
        }
    }
}