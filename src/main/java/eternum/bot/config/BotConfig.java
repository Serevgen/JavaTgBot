package eternum.bot.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Класс для загрузки конфигурации бота из properties файла
 */
public class BotConfig {
    private static final String CONFIG_FILE = "config.properties";
    private final Properties properties;

    public BotConfig() {
        properties = new Properties();
        loadConfig();
    }

    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Не найден файл конфигурации: " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки конфигурации", e);
        }
    }

    public String getBotToken() {
        String token = properties.getProperty("bot.token");
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Токен бота не найден в конфигурации");
        }
        return token.trim();
    }

    public String getBotUsername() {
        return properties.getProperty("bot.username", "MySimpleBot");
    }
}