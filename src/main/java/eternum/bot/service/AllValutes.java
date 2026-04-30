package eternum.bot.service;


import eternum.bot.model.Currency;

import java.util.List;

public class AllValutes {
    private static String valutes;

    public static String rates() {


        List<Currency> currencies = CbrApiClient.listOfValutes();

        currencies.forEach((currency) ->
                valutes += currency.getName() + " (" + currency.getCharCode() + ") - " + currency.getValue() + "\n"
        );
        return valutes.substring(4);
    }
}
