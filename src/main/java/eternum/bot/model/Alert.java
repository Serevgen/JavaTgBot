package eternum.bot.model;

public class Alert {
    private final Long chatId;
    private final String pair;
    private final double targetRate;
    private final boolean triggerIfGreater;

    public Alert(Long chatId, String pair, double currentRate, double targetRate) {
        if (currentRate == targetRate) {
            throw new IllegalArgumentException("Целевой курс не может быть равен текущему.");
        }
        this.chatId = chatId;
        this.pair = pair.toUpperCase();
        this.targetRate = targetRate;
        this.triggerIfGreater = targetRate > currentRate;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getPair() {
        return pair;
    }

    public double getTargetRate() {
        return targetRate;
    }

    public boolean isTriggered(double currentRate) {
        if (triggerIfGreater) {
            return currentRate > targetRate;
        } else {
            return currentRate < targetRate;
        }
    }
}