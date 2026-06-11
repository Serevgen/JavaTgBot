package eternum.bot.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import eternum.bot.SimpleTelegramBot;
import eternum.bot.model.Alert;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AlertManager {

    private static final ConcurrentHashMap<String, List<Alert>> alerts = new ConcurrentHashMap<>();
    private static String alertsFile = "alerts.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final SimpleTelegramBot botInstance;

    static {
        loadAlerts();
    }

    public AlertManager(SimpleTelegramBot botInstance) {
        this.botInstance = botInstance;
    }

    public static void setAlertsFileForTest(String filePath) {
        alertsFile = filePath;
    }

    public static void addAlert(Alert alert) {
        List<Alert> pairAlerts = alerts.computeIfAbsent(alert.getPair(), k -> new CopyOnWriteArrayList<>());
        pairAlerts.removeIf(a -> a.getChatId().equals(alert.getChatId()));
        pairAlerts.add(alert);
        saveAlerts();
    }

    public void checkAlertsAndNotify(CurrencyGraphManager graphManager) {
        AtomicBoolean hasChanges = new AtomicBoolean(false);

        alerts.entrySet().parallelStream().forEach(entry -> {
            String pair = entry.getKey();
            List<Alert> pairAlerts = entry.getValue();

            if (pairAlerts.isEmpty()) return;

            double currentRate = graphManager.getCalculatedRate(pair);
            if (currentRate == -1.0) return;

            List<Alert> triggeredAlerts = checkAlerts(pairAlerts, currentRate);

            if (!triggeredAlerts.isEmpty()) {
                pairAlerts.removeAll(triggeredAlerts);
                sendNotifications(triggeredAlerts, currentRate);
                hasChanges.set(true);
            }
        });

        if (hasChanges.get()) {
            saveAlerts();
        }
    }

    private List<Alert> checkAlerts(List<Alert> pairAlerts, double currentRate) {
        List<Alert> triggered = new ArrayList<>();
        for (Alert alert : pairAlerts) {
            if (alert.isTriggered(currentRate)) {
                triggered.add(alert);
            }
        }
        return triggered;
    }

    private void sendNotifications(List<Alert> triggeredAlerts, double currentRate) {
        for (Alert alert : triggeredAlerts) {
            SendMessage message = new SendMessage();
            message.setChatId(alert.getChatId().toString());
            message.setText("Alarm! Текущий курс " + alert.getPair() + " составляет: " + String.format("%.4f", currentRate));

            try {
                botInstance.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private static synchronized void saveAlerts() {
        try (FileWriter writer = new FileWriter(alertsFile)) {
            GSON.toJson(alerts, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadAlerts() {
        File file = new File(alertsFile);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<ConcurrentHashMap<String, List<Alert>>>() {}.getType();
            ConcurrentHashMap<String, List<Alert>> loaded = GSON.fromJson(reader, type);

            if (loaded != null) {
                alerts.clear();
                loaded.forEach((k, v) -> alerts.put(k, new CopyOnWriteArrayList<>(v)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clear() {
        alerts.clear();
    }

    public static List<Alert> getAlerts(String pair) {
        return alerts.get(pair);
    }
}