package eternum.bot.service;

import eternum.bot.model.Currency;
import java.util.*;

public class CurrencyGraphManager {
    private final Map<String, Double> exchangeRates = new HashMap<>();
    private final Map<String, List<String>> adjList = new HashMap<>();
    private final Map<String, Integer> inDegree = new HashMap<>();
    private final Map<String, CrossRateFormula> crossRateDependencies = new HashMap<>();

    // Вспомогательный класс для хранения формулы
    private static class CrossRateFormula {
        String base1;
        String base2;
        boolean isMultiply; // true: rate1 * rate2, false: rate1 / rate2

        CrossRateFormula(String base1, String base2, boolean isMultiply) {
            this.base1 = base1;
            this.base2 = base2;
            this.isMultiply = isMultiply;
        }
    }

    public void clear() {
        exchangeRates.clear();
        adjList.clear();
        inDegree.clear();
        crossRateDependencies.clear();
    }

    private void addBaseRate(String pair, double rate) {
        exchangeRates.put(pair, rate);
        inDegree.putIfAbsent(pair, 0);
        adjList.putIfAbsent(pair, new ArrayList<>());
    }

    public void initBaseRatesFromCbr(List<Currency> cbrCurrencies) {
        addBaseRate("RUB/RUB", 1.0);
        for (Currency currency : cbrCurrencies) {
            addBaseRate(currency.getCharCode() + "/RUB", currency.getUnitRate());
        }
    }

    public void initBaseRatesFromCrypto(List<Currency> cryptoCurrencies) {
        for (Currency currency : cryptoCurrencies) {
            addBaseRate(currency.getCharCode() + "/USD", currency.getUnitRate());
        }
    }

    // Добавлен флаг isMultiply для вариативности формул
    public void addCrossRateTarget(String targetPair, String base1, String base2, boolean isMultiply) {
        adjList.putIfAbsent(base1, new ArrayList<>());
        adjList.putIfAbsent(base2, new ArrayList<>());

        adjList.get(base1).add(targetPair);
        adjList.get(base2).add(targetPair);


        inDegree.put(targetPair, inDegree.getOrDefault(targetPair, 0) + 2);
        crossRateDependencies.put(targetPair, new CrossRateFormula(base1, base2, isMultiply));
    }

    public void calculateWithTopologicalSort() {
        Queue<String> queue = new LinkedList<>();

        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) queue.add(entry.getKey());
        }

        while (!queue.isEmpty()) {
            String currentPair = queue.poll();

            if (crossRateDependencies.containsKey(currentPair)) {
                CrossRateFormula formula = crossRateDependencies.get(currentPair);
                double rate1 = exchangeRates.getOrDefault(formula.base1, 0.0);
                double rate2 = exchangeRates.getOrDefault(formula.base2, 0.0);

                if (rate1 > 0 && rate2 > 0) {
                    double result = formula.isMultiply ? (rate1 * rate2) : (rate1 / rate2);
                    exchangeRates.put(currentPair, result);
                }
            }

            if (adjList.containsKey(currentPair)) {
                for (String neighbor : adjList.get(currentPair)) {
                    int currentInDegree = inDegree.get(neighbor) - 1;
                    inDegree.put(neighbor, currentInDegree);

                    if (currentInDegree == 0) {
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    public double getCalculatedRate(String pair) {
        return exchangeRates.getOrDefault(pair, -1.0);
    }
}