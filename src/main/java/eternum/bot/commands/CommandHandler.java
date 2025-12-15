package eternum.bot.commands;

import eternum.bot.service.AllValutes;
import eternum.bot.service.CbrApiClient;
import eternum.bot.service.CharCodeValute;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import eternum.bot.service.CbrApiClient;
/**
 * Класс для обработки команд бота
 */
public class CommandHandler {

    /**
     * Обрабатывает входящие команды и возвращает ответ
     */
    public SendMessage handleCommand(Message message) {
        String command = message.getText();
        Long chatId = message.getChatId();


        SendMessage response = new SendMessage();
        response.setChatId(chatId.toString());

        switch (command) {
            case "/start":
                response.setText(handleStartCommand());
                break;
            case "/help":
                response.setText(handleHelpCommand());
                break;
            case "/authors":
                response.setText(handleAuthorsCommand());
                break;
            case "/about":
                response.setText(handleAboutCommand());
                break;
            case "/curs":
                response.setText(handleValuteCommand());
                break;
            default:
                if (command.startsWith("/help ")) {
                    String specificCommand = command.substring(6);
                    response.setText(handleSpecificHelp(specificCommand));
                } else if (command.startsWith("/curs ")) {
                    String specificCommand = command.substring(6);
                    response.setText(handleSpecificCurs(specificCommand));
                }
                else {
                    response.setText("Неизвестная команда. Используйте /help для списка команд.");
                }
        }

        return response;
    }

    private String handleStartCommand() {
        return "Добро пожаловать!\n\n" +
                "Это простой телеграм бот, созданный на Java.\n" +
                "Используйте /help для просмотра доступных команд.";
    }

    private String handleHelpCommand() {
        return "Доступные команды:\n\n" +
                "/start - Начать работу с ботом\n" +
                "/help - Показать это сообщение\n" +
                "/help <command> - Помощь по конкретной команде\n" +
                "/authors - Информация об авторах\n" +
                "/about - О боте\n\n" +
                "Пример: /help start - покажет помощь по команде start";
    }

    private String handleAuthorsCommand() {
        return "Авторы: Овчинников Александр\n" +
                "Сергеев Максим\n";
    }

    private String handleAboutCommand() {
        return "Телеграм бот, который пока что может отвечать на некоторые команды. " +
                "В будущем функционал будет расширен!";
    }

    private String handleSpecificHelp(String command) {
        switch (command) {
            case "start":
                return "Команда /start:\nИнициализирует бота и выводит приветственное сообщение.";
            case "help":
                return "Команда /help:\nПоказывает список всех доступных команд или помощь по конкретной команде.";
            case "authors":
                return "Команда /authors:\nПоказывает информацию о разработчиках бота.";
            case "about":
                return "Команда /about:\nРассказывает о назначении и возможностях бота.";
            default:
                return "Неизвестная команда: " + command + "\nИспользуйте /help для списка команд.";
        }
    }

    private String handleValuteCommand() {
        return AllValutes.rates();
    }

    private String handleSpecificCurs(String command) {
        return CharCodeValute.CodeValute(command);
    }
}
