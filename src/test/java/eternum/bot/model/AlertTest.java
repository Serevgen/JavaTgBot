package eternum.bot.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertTest {

    @Test
    void testAlertTriggerWaitGrowth() {
        Alert alert = new Alert(1L, "USD/RUB", 90.0, 100.0);
        assertFalse(alert.isTriggered(95.0));
        assertFalse(alert.isTriggered(100.0));
        assertTrue(alert.isTriggered(105.0));
    }

    @Test
    void testAlertTriggerWaitDrop() {
        Alert alert = new Alert(1L, "USD/RUB", 100.0, 90.0);
        assertFalse(alert.isTriggered(95.0));
        assertFalse(alert.isTriggered(90.0));
        assertTrue(alert.isTriggered(85.0));
    }

    @Test
    void testAlertThrowsExceptionIfRatesEqual() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Alert(1L, "USD/RUB", 100.0, 100.0);
        });

        assertEquals("Целевой курс не может быть равен текущему.", exception.getMessage());
    }
}