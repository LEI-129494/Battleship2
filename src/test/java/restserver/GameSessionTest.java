package restserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

    private GameSession session;
    private final String gameId = "test-game-id";
    private final String playerName = "Tester";
    private final String callbackUrl = "http://localhost:9090";

    @BeforeEach
    void setUp() {
        session = new GameSession(gameId, playerName, callbackUrl);
    }

    @Test
    @DisplayName("Deve inicializar a sessão com os valores corretos")
    void testConstructorAndGetters() {
        assertEquals(gameId, session.getGameId());
        assertEquals(playerName, session.getPlayerName());
        assertEquals(callbackUrl, session.getCallbackUrl());
        assertNotNull(session.getGame(), "O jogo deve ser instanciado");
        assertEquals(3, session.getShotsPerTurn(), "O número de tiros padrão é 3");

        assertFalse(session.isGameOver(), "O jogo não deve começar terminado");
        assertNull(session.getWinner(), "Não deve haver vencedor no início");
    }

    @Test
    @DisplayName("Deve terminar o jogo com vitória da IA")
    void testMarkAiWins() {
        session.markAiWins();

        assertTrue(session.isGameOver());
        assertEquals("AI_WINS", session.getWinner());
    }

    @Test
    @DisplayName("Deve terminar o jogo com vitória do Aluno")
    void testMarkStudentWins() {
        session.markStudentWins();

        assertTrue(session.isGameOver());
        assertEquals("STUDENT_WINS", session.getWinner());
    }
}