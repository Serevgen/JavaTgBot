package eternum.bot.model;

import com.google.gson.JsonObject;

public class Currency {
    private final String charCode;
    private final String name;
    private final double unitRate;

    public Currency(String charCode, String name, double unitRate) {
        this.charCode = charCode;
        this.name = name;
        this.unitRate = unitRate;
    }

    public String getCharCode() { return charCode; }
    public String getName() { return name; }
    public double getUnitRate() { return unitRate; }

    public static Currency fromCbrJson(JsonObject json) {
        String charCode = json.get("CharCode").getAsString();
        String name = json.get("Name").getAsString();
        double value = json.get("Value").getAsDouble();
        int nominal = json.get("Nominal").getAsInt();

        return new Currency(charCode, name, nominal == 0 ? 0.0 : value / nominal);
    }

    public static Currency fromBinanceJson(JsonObject json) {
        String symbol = json.get("symbol").getAsString();
        String charCode = symbol.replace("USDT", "");
        double price = json.get("price").getAsDouble();

        return new Currency(charCode, charCode, price);
    }
}