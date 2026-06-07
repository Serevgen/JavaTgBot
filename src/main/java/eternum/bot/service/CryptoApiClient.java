package eternum.bot.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import eternum.bot.model.Currency;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CryptoApiClient {
    private static final String BINANCE_API = "https://api.binance.com/api/v3/ticker/price";

    public static List<Currency> listOfCrypto() {
        try {
            String json;
            try (InputStream in = new URL(BINANCE_API).openStream()) {
                json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }

            JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
            List<Currency> cryptoCurrencies = new ArrayList<>();

            for (JsonElement element : jsonArray) {
                String symbol = element.getAsJsonObject().get("symbol").getAsString();
                
                // Забираем только нужные пары к USDT, чтобы не засорять память
                if (symbol.endsWith("USDT") && (symbol.equals("BTCUSDT") || symbol.equals("ETHUSDT") || symbol.equals("BNBUSDT") || symbol.equals("SOLUSDT"))) {
                    cryptoCurrencies.add(Currency.fromBinanceJson(element.getAsJsonObject()));
                }
            }
            return cryptoCurrencies;
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке крипты: " + e.getMessage());
            return new ArrayList<>(); // Возвращаем пустой список, чтобы бот не падал при сбоях API
        }
    }
}