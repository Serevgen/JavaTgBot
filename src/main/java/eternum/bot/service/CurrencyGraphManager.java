package eternum.bot.service;

import eternum.bot.model.Currency;

import java.util.*;

public class CurrencyGraphManager {

    // Хранит актуальные вычисленные курсы
    private final Map<String, Double> exchangeRates = new HashMap<>();

    // Список смежности
    private final Map<String, List<String>> adjList = new HashMap<>();

    // Входящая степень (сколько зависимостей нужно разрешить узлу перед вычислением)
    private final Map<String, Integer> inDegree = new HashMap<>();

    // Формулы для вычисления кросс-курсов
    private final Map<String, String[]> crossRateDependencies = new HashMap<>();

    public void initBaseRatesFromCbr(List<Currency> cbrCurrencies) {
        exchangeRates.put("RUB/RUB", 1.0);
        inDegree.put("RUB/RUB", 0);

        for (Currency currency : cbrCurrencies) {
            String pair = currency.getCharCode() + "/RUB";
            double actualRate = currency.getUnitRate();

            exchangeRates.put(pair, actualRate);
            inDegree.put(pair, 0);
            adjList.putIfAbsent(pair, new ArrayList<>());
        }
    }

    public void addCrossRateTarget(String targetPair, String base1, String base2) {
        // Создаем связи: base1 -> targetPair и base2 -> targetPair
        adjList.putIfAbsent(base1, new ArrayList<>());
        adjList.putIfAbsent(base2, new ArrayList<>());

        adjList.get(base1).add(targetPair);
        adjList.get(base2).add(targetPair);

        // Увеличиваем счетчик зависимостей для целевой пары
        inDegree.put(targetPair, inDegree.getOrDefault(targetPair, 0) + 2);

        crossRateDependencies.put(targetPair, new String[]{base1, base2});
    }

    public void calculateWithTopologicalSort() {
        Queue<String> queue = new LinkedList<>();

        // Помещаем в очередь все узлы, которые не имеют зависимостей
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        // Алгоритм Кана
        while (!queue.isEmpty()) {
            String currentPair = queue.poll();

            if (crossRateDependencies.containsKey(currentPair)) {
                String[] deps = crossRateDependencies.get(currentPair);
                double rate1 = exchangeRates.get(deps[0]);
                double rate2 = exchangeRates.get(deps[1]);
                double crossRate = rate1 / rate2;
                exchangeRates.put(currentPair, crossRate);

            }

            // Уменьшаем счетчик зависимостей у всех соседних узлов, которые ждут этот курс
            if (adjList.containsKey(currentPair)) {
                for (String neighbor : adjList.get(currentPair)) {
                    int currentInDegree = inDegree.get(neighbor) - 1;
                    inDegree.put(neighbor, currentInDegree);

                    // Если все зависимости разрешены, добавляем в очередь на вычисление
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
