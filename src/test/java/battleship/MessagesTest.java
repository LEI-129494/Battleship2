package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import static org.junit.jupiter.api.Assertions.*;

class MessagesTest {

    private Locale defaultLocale;

    @BeforeEach
    void setUp() {
        defaultLocale = Locale.getDefault();
        Messages.init(Locale.of("pt"));
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(defaultLocale);
        Messages.init(defaultLocale);
    }

    @Test
    @DisplayName("Init: Null locale should fallback to JVM default locale")
    void init1() {
        Messages.init(null);
        assertEquals(defaultLocale, Messages.getCurrentLocale(),
                "Error: expected JVM default locale but got " + Messages.getCurrentLocale());
    }

    @Test
    @DisplayName("Init: Valid locale 'en' should load the English bundle successfully")
    void init2() {
        Locale targetLocale = Locale.of("en");
        Messages.init(targetLocale);
        assertEquals(targetLocale, Messages.getCurrentLocale(),
                "Error: expected 'en' locale but got " + Messages.getCurrentLocale());
    }

    @Test
    @DisplayName("Init: Unsupported locale should trigger MissingResourceException and fallback to 'pt'")
    void init3() {
        Locale.setDefault(Locale.of("xx", "XX"));
        Messages.init(Locale.of("fr"));
        assertEquals(Locale.of("pt"), Messages.getCurrentLocale(),
                "Error: expected fallback to 'pt' but got " + Messages.getCurrentLocale());
    }

    @Test
    @DisplayName("GetCurrentLocale: Should return the currently active locale")
    void getCurrentLocale() {
        Locale current = Messages.getCurrentLocale();
        assertEquals(Locale.of("pt"), current,
                "Error: expected 'pt' locale but got " + current);
    }

    @Test
    @DisplayName("Get: Should return message string for an existing key")
    void get() {
        String key = "app.title";
        String result = Messages.get(key);
        assertEquals("***  Battleship  ***", result,
                "Error: expected the app title but got " + result);
    }

    @Test
    @DisplayName("Get: Should throw MissingResourceException for an unknown key")
    void getException() {
        String invalidKey = "this.key.does.not.exist.12345";
        assertThrows(MissingResourceException.class, () -> {
            Messages.get(invalidKey);
        }, "Error: expected MissingResourceException for invalid key");
    }

    @Test
    @DisplayName("Format: Should format message with positional arguments")
    void format() {
        String key = "move.line";
        String result = Messages.format(key, 1, "A1");
        assertEquals("Jogada n.1 -> A1", result,
                "Error: expected 'Jogada n.1 -> A1' but got " + result);
    }

    @Test
    @DisplayName("Format Map: Should format message with named arguments")
    void testFormat() {
        String key = "fleet.status";

        Map<String, Object> args = new HashMap<>();
        args.put("floating", 2);
        args.put("sunk", 1);

        String result = Messages.format(key, args);
        assertEquals("Estado da Frota: 2 a flutuar, 1 afundado!", result,
                "Error: expected formatted fleet status but got " + result);
    }
}