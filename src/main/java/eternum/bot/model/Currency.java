package eternum.bot.model;

import com.google.gson.annotations.SerializedName;

public class Currency {

    @SerializedName("NumCode")
    private String numCode;

    @SerializedName("CharCode")
    private String charCode;

    @SerializedName("Nominal")
    private int nominal;

    @SerializedName("Name")
    private String name;

    @SerializedName("Value")
    private double value;

    public Currency() {}

    public String getName() {
        return name;
    }

    public String getCharCode() {
        return charCode;
    }

    public int getNominal() {
        return nominal;
    }

    public double getValue() {
        return value;
    }


    // Вычисляет реальный курс за 1 единицу валюты,

    public double getUnitRate() {
        if (nominal == 0) return 0.0; // Защита от деления на ноль
        return value / nominal;
    }
}