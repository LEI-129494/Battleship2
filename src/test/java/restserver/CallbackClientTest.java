package restserver;

import battleship.IPosition;
import battleship.Position;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CallbackClientTest {

    private CallbackClient client;
    private HttpServer server;
    private GameSession session;

    @BeforeEach
    void setUp() throws IOException {
        // Arranca um servidor HTTP local numa porta aleatória (0)
        server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        server.start();

        client = new CallbackClient();
        session = new GameSession("game-123", "Tester", "http://localhost:" + port);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("Deve enviar tiros com sucesso e detetar fim de jogo")
    void testSendShotsSuccessGameOver() {
        server.createContext("/game/game-123/shots", exchange -> {
            String response = "{\"results\":[{\"row\":\"A\",\"column\":1,\"outcome\":\"SUNK\"}],\"shipsRemaining\":0,\"gameStatus\":\"GAME_OVER\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        // CORREÇÃO: 'A' (char) em vez de "A" (String)
        List<IPosition> shots = List.of(new Position('A', 1));
        boolean result = client.sendShots(session, shots);

        assertTrue(result, "O envio deve ter sucesso");
        assertTrue(session.isGameOver(), "O jogo deve terminar");
        assertEquals("AI_WINS", session.getWinner());
    }

    @Test
    @DisplayName("Deve enviar tiros com sucesso mas o jogo continua")
    void testSendShotsSuccessOngoing() {
        server.createContext("/game/game-123/shots", exchange -> {
            String response = "{\"results\":[{\"row\":\"A\",\"column\":1,\"outcome\":\"HIT\"}],\"shipsRemaining\":5,\"gameStatus\":\"ONGOING\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        List<IPosition> shots = List.of(new Position('A', 1));
        boolean result = client.sendShots(session, shots);

        assertTrue(result, "O envio deve ter sucesso");
        assertFalse(session.isGameOver(), "O jogo NÃO deve terminar");
    }

    @Test
    @DisplayName("Deve retornar false se o servidor do aluno der erro HTTP (ex: 400)")
    void testSendShotsHttpError() {
        server.createContext("/game/game-123/shots", exchange -> {
            String response = "Bad Request";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        boolean result = client.sendShots(session, List.of(new Position('A', 1)));

        assertFalse(result, "Deve retornar false para status != 2xx");
    }

    @Test
    @DisplayName("Deve retornar false se a resposta do aluno for JSON inválido")
    void testSendShotsInvalidJson() {
        server.createContext("/game/game-123/shots", exchange -> {
            String response = "Isso nao e um json";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        boolean result = client.sendShots(session, List.of(new Position('A', 1)));

        assertFalse(result, "Deve retornar false se falhar o parse do JSON");
    }

    @Test
    @DisplayName("Deve retornar false se falhar a ligação ao servidor do aluno")
    void testSendShotsConnectionFailed() {
        server.stop(0);

        boolean result = client.sendShots(session, List.of(new Position('A', 1)));

        assertFalse(result, "Deve retornar false em caso de IOException");
    }

    @Test
    @DisplayName("Deve processar resultados com null, INVALID e REPEATED")
    void testSendShotsEdgeCases() {
        // Testa o caso em que results é null e depois status não é GAME_OVER mas shipsRemaining é 0
        server.createContext("/game/game-123/shots", exchange -> {
            String response = "{\"results\":null,\"shipsRemaining\":0,\"gameStatus\":\"ONGOING\"}";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        boolean result = client.sendShots(session, List.of(new Position('A', 1)));
        assertTrue(result);
        assertTrue(session.isGameOver(), "O jogo deve acabar porque shipsRemaining == 0");
    }

    @Test
    @DisplayName("Deve processar resultados INVALID e REPEATED")
    void testSendShotsInvalidAndRepeated() {
        server.createContext("/game/game-123/shots", exchange -> {
            String response = "{\"results\":[" +
                    "{\"row\":\"A\",\"column\":1,\"outcome\":\"INVALID\"}," +
                    "{\"row\":\"B\",\"column\":2,\"outcome\":\"REPEATED\"}" +
                    "],\"shipsRemaining\":5,\"gameStatus\":\"ONGOING\"}";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        // Passamos duas posições para bater certo com os dois resultados
        List<IPosition> shots = List.of(new Position('A', 1), new Position('B', 2));
        boolean result = client.sendShots(session, shots);
        assertTrue(result);
    }
}