package restserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameRegistryTest {

    private GameRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new GameRegistry();
    }

    @Test
    @DisplayName("Deve criar e armazenar uma nova sessão com sucesso")
    void testCreateAndGetSession() {
        GameSession session = registry.createSession("Player1", "http://localhost:8080");

        assertNotNull(session, "A sessão devolvida não deve ser nula");
        assertNotNull(session.getGameId(), "O gameId deve ser gerado");

        GameSession fetched = registry.getSession(session.getGameId());
        assertNotNull(fetched, "A sessão deve ser encontrada no registry");
        assertEquals("Player1", fetched.getPlayerName());
        assertEquals("http://localhost:8080", fetched.getCallbackUrl());
    }

    @Test
    @DisplayName("Deve devolver null ao procurar uma sessão inexistente")
    void testGetSessionNotFound() {
        assertNull(registry.getSession("id-que-nao-existe"));
    }

    @Test
    @DisplayName("Deve remover uma sessão corretamente")
    void testRemoveSession() {
        GameSession session = registry.createSession("Player2", "http://localhost:8081");
        String gameId = session.getGameId();

        assertNotNull(registry.getSession(gameId));

        registry.removeSession(gameId);

        assertNull(registry.getSession(gameId), "A sessão deve ter sido removida");
    }
}