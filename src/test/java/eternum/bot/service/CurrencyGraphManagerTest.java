package eternum.bot.service;

import eternum.bot.model.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyGraphManagerTest {

    private CurrencyGraphManager graphManager;

    @BeforeEach
    void setUp() {
        graphManager = new CurrencyGraphManager();
    }

    @Test
    void testTopologicalSortCalculatesCorrectly() {
        // 1. Имитируем данные от API
        List<Currency> cbrData = List.of(
                new Currency("USD", "Доллар", 100.0),
                new Currency("EUR", "Евро", 110.0)
        );
        
        List<Currency> cryptoData = List.of(
                new Currency("BTC", "Bitcoin", 50000.0)
        );

        // 2. Инициализируем базовые курсы
        graphManager.initBaseRatesFromCbr(cbrData);
        graphManager.initBaseRatesFromCrypto(cryptoData);

        // 3. Выстраиваем зависимости (как в BotExchangeService)
        graphManager.addCrossRateTarget("USD/EUR", "USD/RUB", "EUR/RUB", false);

        graphManager.addCrossRateTarget("BTC/RUB", "BTC/USD", "USD/RUB", true);

        graphManager.addCrossRateTarget("BTC/EUR", "BTC/RUB", "EUR/RUB", false);

        graphManager.calculateWithTopologicalSort();

        assertEquals(100.0, graphManager.getCalculatedRate("USD/RUB"));
        assertEquals(50000.0, graphManager.getCalculatedRate("BTC/USD"));

        // Кросс-курсы 1 уровня
        assertEquals(100.0 / 110.0, graphManager.getCalculatedRate("USD/EUR"), 0.0001);
        assertEquals(50000.0 * 100.0, graphManager.getCalculatedRate("BTC/RUB"), 0.0001); // 5 миллионов рублей

        // Кросс-курс 2 уровня
        double expectedBtcToEur = (50000.0 * 100.0) / 110.0;
        assertEquals(expectedBtcToEur, graphManager.getCalculatedRate("BTC/EUR"), 0.0001);
    }

    @Test
    void testClearResetsGraph() {
        graphManager.initBaseRatesFromCbr(List.of(new Currency("USD", "Доллар", 100.0)));
        assertEquals(100.0, graphManager.getCalculatedRate("USD/RUB"));

        graphManager.clear();

        assertEquals(-1.0, graphManager.getCalculatedRate("USD/RUB"));
    }
}