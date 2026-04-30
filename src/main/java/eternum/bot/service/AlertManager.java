package eternum.bot.service;

import eternum.bot.SimpleTelegramBot;
import eternum.bot.model.Alert;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AlertManager {

    private static final List<Alert> alerts = new CopyOnWriteArrayList<>();
    private final SimpleTelegramBot botInstance;

    public AlertManager(SimpleTelegramBot botInstance) {
        this.botInstance = botInstance;
    }

    public static void addAlert(Alert alert) {
        alerts.add(alert);
    }

    public void checkAlertsAndNotify(CurrencyGraphManager graphManager) {
        for (Alert alert : alerts) {
            double currentRate = graphManager.getCalculatedRate(alert.getPair());

            if (currentRate != -1.0 && alert.isTriggered(currentRate)) {
                sendNotification(alert.getChatId(), alert.getPair(), currentRate);
                alerts.remove(alert);
            }
        }
    }

    private void sendNotification(Long chatId, String pair, double currentRate) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Alarm! Текущий курс " + pair + " составляет: " + String.format("%.4f", currentRate));

        try {
            botInstance.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}