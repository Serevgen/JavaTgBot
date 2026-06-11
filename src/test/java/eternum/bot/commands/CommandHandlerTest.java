package eternum.bot.commands;

import eternum.bot.service.AlertManager;
import eternum.bot.service.AllValutes;
import eternum.bot.service.BotExchangeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandHandlerTest {

    private CommandHandler commandHandler;
    private Message testMessage;
    private final Long TEST_CHAT_ID = 123456789L;

    @BeforeEach
    void setUp() {
        commandHandler = new CommandHandler();

        testMessage = new Message();
        Chat chat = new Chat();
        chat.setId(TEST_CHAT_ID);
        testMessage.setChat(chat);
    }

    @Test
    void testStartCommand() {
        testMessage.setText("/start");

        SendMessage response = commandHandler.handleCommand(testMessage);

        assertEquals(TEST_CHAT_ID.toString(), response.getChatId());
        assertTrue(response.getText().contains("Добро пожаловать!"));
    }

    @Test
    void testUnknownCommand() {
        testMessage.setText("/some_unknown_command");

        SendMessage response = commandHandler.handleCommand(testMessage);

        assertEquals(TEST_CHAT_ID.toString(), response.getChatId());
        assertEquals("Неизвестная команда. Используйте /help для списка команд.", response.getText());
    }

    @Test
    void testSpecificHelpCommand() {
        testMessage.setText("/help authors");

        SendMessage response = commandHandler.handleCommand(testMessage);

        assertTrue(response.getText().contains("Команда /authors:"));
        assertTrue(response.getText().contains("информацию о разработчиках"));
    }

    @Test
    void testValuteCommand_CallsAllValutes() {
        testMessage.setText("/rate");

        try (MockedStatic<AllValutes> mockedAllValutes = mockStatic(AllValutes.class)) {
            mockedAllValutes.when(AllValutes::rates).thenReturn("Тестовый список валют");

            SendMessage response = commandHandler.handleCommand(testMessage);

            assertEquals("Тестовый список валют", response.getText());
        }
    }

    @Test
    void testSpecificCursCommand_ValidPair() {
        testMessage.setText("/rate USD/EUR");

        try (MockedStatic<BotExchangeService> mockedService = mockStatic(BotExchangeService.class)) {
            mockedService.when(() -> BotExchangeService.getCurrentRate("USD/EUR")).thenReturn(0.95);

            SendMessage response = commandHandler.handleCommand(testMessage);

            assertTrue(response.getText().contains("0"));
            assertTrue(response.getText().contains("95"));
        }
    }

    @Test
    void testSpecificCursCommand_AppendsRubToSingleCurrency() {
        testMessage.setText("/rate USD");

        try (MockedStatic<BotExchangeService> mockedService = mockStatic(BotExchangeService.class)) {
            mockedService.when(() -> BotExchangeService.getCurrentRate("USD/RUB")).thenReturn(90.5);

            SendMessage response = commandHandler.handleCommand(testMessage);

            assertTrue(response.getText().contains("Курс USD/RUB: 90"));
        }
    }

    @Test
    void testSpecificCursCommand_NotFound() {
        testMessage.setText("/rate xxx");

        try (MockedStatic<BotExchangeService> mockedService = mockStatic(BotExchangeService.class)) {
            mockedService.when(() -> BotExchangeService.getCurrentRate("XXX/RUB")).thenReturn(-1.0);

            SendMessage response = commandHandler.handleCommand(testMessage);

            assertTrue(response.getText().contains("Не удалось найти курс для: XXX/RUB"));
        }
    }

    @Test
    void testAlertCommand_Success() {
        testMessage.setText("/alert USD/RUB 100.0");

        try (MockedStatic<BotExchangeService> mockedService = mockStatic(BotExchangeService.class);
             MockedStatic<AlertManager> mockedAlertManager = mockStatic(AlertManager.class)) {

            mockedService.when(() -> BotExchangeService.getCurrentRate("USD/RUB")).thenReturn(95.0);

            SendMessage response = commandHandler.handleCommand(testMessage);

            assertTrue(response.getText().contains("Уведомление установлено!"));
            assertTrue(response.getText().contains("100.0"));

            mockedAlertManager.verify(() -> AlertManager.addAlert(any()), times(1));
        }
    }

    @Test
    void testAlertCommand_InvalidFormat() {
        testMessage.setText("/alert USD/RUB");

        SendMessage response = commandHandler.handleCommand(testMessage);

        assertTrue(response.getText().contains("Неверный формат"));
    }

    @Test
    void testAlertCommand_NotANumber() {
        testMessage.setText("/alert USD/RUB сто_рублей");

        SendMessage response = commandHandler.handleCommand(testMessage);

        assertTrue(response.getText().contains("Ошибка: курс должен быть числом"));
    }

    @Test
    void testAlertCommand_CurrencyNotFound() {
        testMessage.setText("/alert FAKE/RUB 100.0");

        try (MockedStatic<BotExchangeService> mockedService = mockStatic(BotExchangeService.class)) {
            mockedService.when(() -> BotExchangeService.getCurrentRate("FAKE/RUB")).thenReturn(-1.0);

            SendMessage response = commandHandler.handleCommand(testMessage);

            assertEquals("Валютная пара FAKE/RUB не найдена.", response.getText());
        }
    }
}