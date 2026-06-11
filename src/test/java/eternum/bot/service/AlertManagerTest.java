package eternum.bot.service;

import eternum.bot.SimpleTelegramBot;
import eternum.bot.model.Alert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlertManagerTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private SimpleTelegramBot mockBot;
    private CurrencyGraphManager mockGraphManager;
    private AlertManager alertManager;
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        mockBot = mock(SimpleTelegramBot.class);
        mockGraphManager = mock(CurrencyGraphManager.class);
        alertManager = new AlertManager(mockBot);

        tempFile = File.createTempFile("alerts_test", ".json");
        AlertManager.setAlertsFileForTest(tempFile.getAbsolutePath());
        AlertManager.clear();
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testAddAlert_OverwritesPreviousAlertForSamePairAndUser() {
        Alert alert1 = new Alert(123L, "USD/RUB", 90.0, 100.0);
        AlertManager.addAlert(alert1);

        Alert alert2 = new Alert(123L, "USD/RUB", 90.0, 110.0);
        AlertManager.addAlert(alert2);

        List<Alert> usdRubAlerts = AlertManager.getAlerts("USD/RUB");

        assertNotNull(usdRubAlerts);
        assertEquals(1, usdRubAlerts.size());
        assertEquals(110.0, usdRubAlerts.get(0).getTargetRate());
    }

    @Test
    void testAddAlert_SavesToFile() throws IOException {
        Alert alert = new Alert(123L, "USD/RUB", 90.0, 100.0);
        AlertManager.addAlert(alert);

        String fileContent = Files.readString(tempFile.toPath());
        assertTrue(fileContent.contains("USD/RUB"));
        assertTrue(fileContent.contains("123"));
        assertTrue(fileContent.contains("100.0"));
    }

    @Test
    void testLoadAlerts_RestoresFromFile() {
        Alert alert = new Alert(123L, "USD/RUB", 90.0, 100.0);
        AlertManager.addAlert(alert);

        AlertManager.clear();
        assertNull(AlertManager.getAlerts("USD/RUB"));

        AlertManager.loadAlerts();

        List<Alert> restoredAlerts = AlertManager.getAlerts("USD/RUB");
        assertNotNull(restoredAlerts);
        assertEquals(1, restoredAlerts.size());
        assertEquals(100.0, restoredAlerts.get(0).getTargetRate());
    }

    @Test
    void testCheckAlertsAndNotify_TriggersCorrectlyAndRemovesAlert() throws TelegramApiException {
        Alert alert = new Alert(123L, "USD/RUB", 90.0, 100.0);
        AlertManager.addAlert(alert);

        when(mockGraphManager.getCalculatedRate("USD/RUB")).thenReturn(105.0);

        alertManager.checkAlertsAndNotify(mockGraphManager);

        ArgumentCaptor<BotApiMethod> messageCaptor = ArgumentCaptor.forClass(BotApiMethod.class);
        verify(mockBot, times(1)).execute(messageCaptor.capture());

        SendMessage sentMessage = (SendMessage) messageCaptor.getValue();
        assertEquals("123", sentMessage.getChatId());
        assertTrue(sentMessage.getText().contains("Alarm!"));
        assertTrue(sentMessage.getText().contains("USD/RUB"));
        assertTrue(sentMessage.getText().contains("105"));

        alertManager.checkAlertsAndNotify(mockGraphManager);
        verify(mockBot, times(1)).execute(any(BotApiMethod.class));

        List<Alert> usdRubAlerts = AlertManager.getAlerts("USD/RUB");
        assertTrue(usdRubAlerts == null || usdRubAlerts.isEmpty());
    }

    @Test
    void testCheckAlertsAndNotify_DoesNotTriggerIfRateNotReached() throws TelegramApiException {
        Alert alert = new Alert(123L, "USD/RUB", 90.0, 100.0);
        AlertManager.addAlert(alert);

        when(mockGraphManager.getCalculatedRate("USD/RUB")).thenReturn(95.0);

        alertManager.checkAlertsAndNotify(mockGraphManager);

        verify(mockBot, never()).execute(any(BotApiMethod.class));
    }

    @Test
    void testCheckAlertsAndNotify_IgnoresUnknownCurrencies() throws TelegramApiException {
        Alert alert = new Alert(123L, "USD/RUB", 90.0, 100.0);
        AlertManager.addAlert(alert);

        when(mockGraphManager.getCalculatedRate("USD/RUB")).thenReturn(-1.0);

        alertManager.checkAlertsAndNotify(mockGraphManager);

        verify(mockBot, never()).execute(any(BotApiMethod.class));
    }
}