package eternum.bot.model;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyTest {

    @Test
    void testFromCbrJson_NormalNominal() {
        JsonObject json = new JsonObject();
        json.addProperty("CharCode", "USD");
        json.addProperty("Name", "Доллар США");
        json.addProperty("Value", 90.50);
        json.addProperty("Nominal", 1);

        Currency currency = Currency.fromCbrJson(json);

        assertEquals("USD", currency.getCharCode());
        assertEquals("Доллар США", currency.getName());
        assertEquals(90.50, currency.getUnitRate(), 0.0001);
    }

    @Test
    void testFromCbrJson_HighNominal() {
        JsonObject json = new JsonObject();
        json.addProperty("CharCode", "AMD");
        json.addProperty("Name", "Армянских драмов");
        json.addProperty("Value", 22.50);
        json.addProperty("Nominal", 100);

        Currency currency = Currency.fromCbrJson(json);

        assertEquals(0.225, currency.getUnitRate(), 0.0001);
    }

    @Test
    void testFromBinanceJson_RemovesUSDT() {
        JsonObject json = new JsonObject();
        json.addProperty("symbol", "BTCUSDT");
        json.addProperty("price", 65000.00);

        Currency currency = Currency.fromBinanceJson(json);

        assertEquals("BTC", currency.getCharCode());
        assertEquals(65000.00, currency.getUnitRate(), 0.0001);
    }
}