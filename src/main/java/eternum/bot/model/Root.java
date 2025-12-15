package eternum.bot.model;

import java.util.List;

public class Root {
    private List<Currency> currencies;

    public Root(List<Currency> currencies) {
        this.currencies = currencies;
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }
}
