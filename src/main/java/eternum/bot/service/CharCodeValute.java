package eternum.bot.service;

import eternum.bot.model.Currency;

import java.util.List;

public class CharCodeValute {
    private static String valute;

    public static String CodeValute(String charCode) {

        List<Currency> currencies = CbrApiClient.listOfValutes();

        for  (Currency currency : currencies) {
            if (currency.getCharCode().equals(charCode)) {
                valute += currency.getName() + " (" + currency.getCharCode() + ") - " + currency.getValue() + "\n";
                break;
            }
        }
        return valute.substring(4);
    }
}
