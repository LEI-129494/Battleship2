package restserver;

import battleship.*;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import restserver.Dtos.*;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    private GameController controller;
    private TestRegistry registry;
    private TestCallbackClient callback;
    private TestAiStrategy aiStrategy;

    private static final String GAME_ID = "test-game-1";

    private static class TestRegistry extends GameRegistry {

        private GameSession testSession;

        @Override
        public GameSession getSession(String gameId) {
            if (GAME_ID.equals(gameId)) {
                return testSession;
            }
            return null;
        }
    }

    private static class TestCallbackClient extends CallbackClient {

        boolean shouldSucceed = true;
        boolean called = false;
        List<IPosition> receivedShots;

        @Override
        public boolean sendShots(
                GameSession session,
                List<IPosition> shots) {

            called = true;
            receivedShots = shots;
            return shouldSucceed;
        }
    }

    private static class TestAiStrategy extends AiShotStrategy {

        boolean called = false;

        @Override
        public List<IPosition> computeShots(IGame game) {
            called = true;

            return List.of(
                    new Position(0, 0),
                    new Position(2, 2),
                    new Position(4, 4)
            );
        }
    }

    private static class TestSession extends GameSession {

        private final IGame testGame;

        TestSession(IGame game) {
            super(GAME_ID, "Player1",
                    "http://localhost:9090");

            testGame = game;
        }

        @Override
        public IGame getGame() {
            return testGame;
        }
    }
    //setup

    @BeforeEach
    void setUp() {
        registry = new TestRegistry();
        callback = new TestCallbackClient();
        aiStrategy = new TestAiStrategy();

        controller = new GameController(
                registry, callback, aiStrategy);
    }

    @AfterEach
    void tearDown() {
        controller = null;
        registry = null;
        callback = null;
        aiStrategy = null;
    }

    //metodos auxiliares
    private RegistrationRequest registration(
            String name, String url) {

        RegistrationRequest request =
                new RegistrationRequest();

        request.playerName = name;
        request.callbackUrl = url;

        return request;
    }

    private ShotPosition position(String row, int column) {
        ShotPosition pos = new ShotPosition();

        pos.row = row;
        pos.column = column;

        return pos;
    }

    private ShotRequest shotRequest(ShotPosition... shots) {
        ShotRequest request = new ShotRequest();
        request.shots = List.of(shots);
        return request;
    }

    private ShotRequest validRequest() {
        return shotRequest(
                position("A", 1),
                position("B", 2),
                position("C", 3));
    }

    private void assertError(
            ResponseEntity<?> response,
            HttpStatus expected,
            String message) {

        assertEquals(expected, response.getStatusCode(),
                "Error: Incorrect HTTP status");

        assertInstanceOf(ErrorResponse.class,
                response.getBody(),
                "Error: Response should contain ErrorResponse");

        ErrorResponse body =
                (ErrorResponse) response.getBody();

        assertEquals(message, body.error,
                "Error: Incorrect error message");
    }

    /**
     * Creates a controlled IGame.
     *
     * The supplied results are returned sequentially
     * whenever fireSingleShot() is called.
     */
    private IGame createGame(
            int remainingShips,
            List<IGame.ShotResult> results,
            List<Boolean> repeatedFlags) {

        final int[] index = {0};

        return (IGame) Proxy.newProxyInstance(
                IGame.class.getClassLoader(),
                new Class<?>[]{IGame.class},
                (proxy, method, args) -> {

                    switch (method.getName()) {

                        case "getRemainingShips":
                            return remainingShips;

                        case "fireSingleShot":
                            repeatedFlags.add((Boolean) args[1]);

                            if (index[0] >= results.size()) {
                                throw new AssertionError(
                                        "Error: Unexpected extra shot");
                            }

                            return results.get(index[0]++);

                        default:
                            throw new UnsupportedOperationException(
                                    "Unexpected method: " +
                                            method.getName());
                    }
                });
    }

    private IGame.ShotResult miss() {
        return new IGame.ShotResult(
                true, false, null, false);
    }

    private IGame gameWithResults(
            int remainingShips,
            IGame.ShotResult... results) {

        return createGame(
                remainingShips,
                List.of(results),
                new ArrayList<>());
    }

    private void prepareSession(IGame game) {
        registry.testSession = new TestSession(game);
    }

    //construtores

    @Test
    @DisplayName("Constructor: Should create controller")
    void testConstructor() {

        assertNotNull(controller,
                "Error: Controller should not be null");
    }

    //registers

    @Test
    @DisplayName("Register: Should reject null player name")
    void register1() {

        ResponseEntity<?> response = controller.register(
                registration(null, "http://localhost:9090"));

        assertError(response, HttpStatus.BAD_REQUEST,
                "playerName is required");
    }

    @Test
    @DisplayName("Register: Should reject blank player name")
    void register2() {

        ResponseEntity<?> response = controller.register(
                registration("   ", "http://localhost:9090"));

        assertError(response, HttpStatus.BAD_REQUEST,
                "playerName is required");
    }

    @Test
    @DisplayName("Register: Should reject null callback URL")
    void register3() {

        ResponseEntity<?> response = controller.register(
                registration("Player1", null));

        assertError(response, HttpStatus.BAD_REQUEST,
                "callbackUrl is required");
    }

    @Test
    @DisplayName("Register: Should reject blank callback URL")
    void register4() {

        ResponseEntity<?> response = controller.register(
                registration("Player1", "   "));

        assertError(response, HttpStatus.BAD_REQUEST,
                "callbackUrl is required");
    }

    @Test
    @DisplayName("Register: Should register a valid player")
    void register5() {

        ResponseEntity<?> response = controller.register(
                registration("Player1", "http://localhost:9090"));

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Error: Valid registration should return HTTP 200");

        RegistrationResponse body =
                assertInstanceOf(RegistrationResponse.class,
                        response.getBody());

        assertAll(
                () -> assertNotNull(body.gameId,
                        "Error: Game ID should not be null"),

                () -> assertEquals("REGISTERED", body.status,
                        "Error: Incorrect registration status"),

                () -> assertEquals(
                        Game.NUMBER_SHOTS,
                        body.shotsPerTurn,
                        "Error: Incorrect shots per turn"),

                () -> assertTrue(
                        body.message.contains("Player1"),
                        "Error: Welcome message should contain player name")
        );
    }

    //disparos recebidos validação

    @Test
    @DisplayName("ReceiveShots: Should reject unknown game")
    void receiveShots1() {

        ResponseEntity<?> response =
                controller.receiveShots(
                        "unknown-game", validRequest());

        assertError(response, HttpStatus.NOT_FOUND,
                "Game not found: unknown-game");
    }

    @Test
    @DisplayName("ReceiveShots: Should reject finished game")
    void receiveShots2() {

        prepareSession(gameWithResults(1));

        registry.testSession.markAiWins();

        ResponseEntity<?> response =
                controller.receiveShots(
                        GAME_ID, validRequest());

        assertError(response, HttpStatus.GONE,
                "Game " + GAME_ID +
                        " is already over. Winner: AI_WINS");
    }

    @Test
    @DisplayName("ReceiveShots: Should reject null shot list")
    void receiveShots3() {

        prepareSession(gameWithResults(1));

        ShotRequest request = new ShotRequest();
        request.shots = null;

        ResponseEntity<?> response =
                controller.receiveShots(GAME_ID, request);

        assertError(response, HttpStatus.BAD_REQUEST,
                "Exactly " + Game.NUMBER_SHOTS +
                        " shots required per turn");
    }

    @Test
    @DisplayName("ReceiveShots: Should reject incorrect shot count")
    void receiveShots4() {

        prepareSession(gameWithResults(1));

        ShotRequest request = shotRequest(
                position("A", 1));

        ResponseEntity<?> response =
                controller.receiveShots(GAME_ID, request);

        assertError(response, HttpStatus.BAD_REQUEST,
                "Exactly " + Game.NUMBER_SHOTS +
                        " shots required per turn");
    }

    @Test
    @DisplayName("ReceiveShots: Should reject null row")
    void receiveShots5() {

        prepareSession(gameWithResults(1));

        ShotRequest request = shotRequest(
                position(null, 1),
                position("B", 2),
                position("C", 3));

        ResponseEntity<?> response =
                controller.receiveShots(GAME_ID, request);

        assertError(response, HttpStatus.BAD_REQUEST,
                "Invalid position: row='null' column=1");
    }

    @Test
    @DisplayName("ReceiveShots: Should reject blank row")
    void receiveShots6() {

        prepareSession(gameWithResults(1));

        ShotRequest request = shotRequest(
                position("   ", 1),
                position("B", 2),
                position("C", 3));

        ResponseEntity<?> response =
                controller.receiveShots(GAME_ID, request);

        assertError(response, HttpStatus.BAD_REQUEST,
                "Invalid position: row='   ' column=1");
    }

    @Test
    @DisplayName("ReceiveShots: Should reject column below 1")
    void receiveShots7() {

        prepareSession(gameWithResults(1));

        ShotRequest request = shotRequest(
                position("A", 0),
                position("B", 2),
                position("C", 3));

        ResponseEntity<?> response =
                controller.receiveShots(GAME_ID, request);

        assertError(response, HttpStatus.BAD_REQUEST,
                "Invalid position: row='A' column=0");
    }

    @Test
    @DisplayName("ReceiveShots: Should reject column above 10")
    void receiveShots8() {

        prepareSession(gameWithResults(1));

        ShotRequest request = shotRequest(
                position("A", 11),
                position("B", 2),
                position("C", 3));

        ResponseEntity<?> response =
                controller.receiveShots(GAME_ID, request);

        assertError(response, HttpStatus.BAD_REQUEST,
                "Invalid position: row='A' column=11");
    }

    // disparos recebidos normal

    @Test
    @DisplayName("ReceiveShots: Should process misses and invoke AI")
    void receiveShots9() {

        prepareSession(gameWithResults(
                5, miss(), miss(), miss()));

        ResponseEntity<?> response =
                controller.receiveShots(
                        GAME_ID, validRequest());

        ShotResponse body =
                assertInstanceOf(ShotResponse.class,
                        response.getBody());

        assertAll(
                () -> assertEquals(HttpStatus.OK,
                        response.getStatusCode(),
                        "Error: Should return HTTP 200"),

                () -> assertEquals(3, body.results.size(),
                        "Error: Should return three results"),

                () -> assertTrue(
                        body.results.stream().allMatch(
                                r -> "MISS".equals(r.outcome)),
                        "Error: All shots should be MISS"),

                () -> assertEquals(5, body.shipsRemaining,
                        "Error: Incorrect remaining ships"),

                () -> assertEquals("ONGOING", body.gameStatus,
                        "Error: Game should remain ongoing"),

                () -> assertNull(body.winner,
                        "Error: Winner should be null"),

                () -> assertTrue(aiStrategy.called,
                        "Error: AI strategy should be called"),

                () -> assertTrue(callback.called,
                        "Error: Callback should be called")
        );
    }

    @Test
    @DisplayName("ReceiveShots: Should handle failed callback")
    void receiveShots10() {

        prepareSession(gameWithResults(
                5, miss(), miss(), miss()));

        callback.shouldSucceed = false;

        ResponseEntity<?> response =
                controller.receiveShots(
                        GAME_ID, validRequest());

        ShotResponse body =
                assertInstanceOf(ShotResponse.class,
                        response.getBody());

        assertAll(
                () -> assertEquals(HttpStatus.OK,
                        response.getStatusCode(),
                        "Error: Callback failure should not prevent HTTP 200"),

                () -> assertEquals("ONGOING", body.gameStatus,
                        "Error: Game should remain ongoing"),

                () -> assertTrue(callback.called,
                        "Error: Callback should have been attempted")
        );
    }

    // disparos recebidos win

    @Test
    @DisplayName("ReceiveShots: Should detect student victory")
    void receiveShots11() {

        prepareSession(gameWithResults(
                0, miss(), miss(), miss()));

        ResponseEntity<?> response =
                controller.receiveShots(
                        GAME_ID, validRequest());

        ShotResponse body =
                assertInstanceOf(ShotResponse.class,
                        response.getBody());

        assertAll(
                () -> assertEquals(HttpStatus.OK,
                        response.getStatusCode(),
                        "Error: Victory should return HTTP 200"),

                () -> assertEquals("GAME_OVER", body.gameStatus,
                        "Error: Game should be finished"),

                () -> assertEquals("STUDENT_WINS", body.winner,
                        "Error: Student should be the winner"),

                () -> assertTrue(
                        registry.testSession.isGameOver(),
                        "Error: Session should be marked as finished"),

                () -> assertFalse(aiStrategy.called,
                        "Error: AI should not fire after student victory"),

                () -> assertFalse(callback.called,
                        "Error: Callback should not run after student victory")
        );
    }

    @Test
    @DisplayName("EvaluateShots: Should return INVALID, REPEATED and MISS")
    void receiveShots12() {

        prepareSession(gameWithResults(
                5,
                new IGame.ShotResult(
                        false, false, null, false),

                new IGame.ShotResult(
                        true, true, null, false),

                miss()
        ));

        ResponseEntity<?> response =
                controller.receiveShots(
                        GAME_ID, validRequest());

        ShotResponse body =
                assertInstanceOf(ShotResponse.class,
                        response.getBody());

        assertAll(
                () -> assertEquals("INVALID",
                        body.results.get(0).outcome,
                        "Error: First result should be INVALID"),

                () -> assertEquals("REPEATED",
                        body.results.get(1).outcome,
                        "Error: Second result should be REPEATED"),

                () -> assertEquals("MISS",
                        body.results.get(2).outcome,
                        "Error: Third result should be MISS")
        );
    }

    @Test
    @DisplayName("EvaluateShots: Should return HIT and SUNK with ship type")
    void receiveShots13() {

        IShip ship = new Caravel(
                Compass.NORTH, new Position(4, 4));

        prepareSession(gameWithResults(
                5,
                new IGame.ShotResult(
                        true, false, ship, false),

                new IGame.ShotResult(
                        true, false, ship, true),

                miss()
        ));

        ResponseEntity<?> response =
                controller.receiveShots(
                        GAME_ID, validRequest());

        ShotResponse body =
                assertInstanceOf(ShotResponse.class,
                        response.getBody());

        assertAll(
                () -> assertEquals("HIT",
                        body.results.get(0).outcome,
                        "Error: First result should be HIT"),

                () -> assertEquals(ship.getCategory(),
                        body.results.get(0).shipType,
                        "Error: HIT should contain ship category"),

                () -> assertEquals("SUNK",
                        body.results.get(1).outcome,
                        "Error: Second result should be SUNK"),

                () -> assertEquals(ship.getCategory(),
                        body.results.get(1).shipType,
                        "Error: SUNK should contain ship category"),

                () -> assertEquals("MISS",
                        body.results.get(2).outcome,
                        "Error: Third result should be MISS")
        );
    }


    @Test
    @DisplayName("EvaluateShots: Should identify repeated positions")
    void receiveShots14() {

        List<Boolean> repeatedFlags = new ArrayList<>();

        IGame game = createGame(
                5,
                List.of(miss(), miss(), miss()),
                repeatedFlags
        );

        prepareSession(game);

        ShotRequest request = shotRequest(
                position("A", 1),
                position("A", 1),
                position("B", 2)
        );

        ResponseEntity<?> response =
                controller.receiveShots(GAME_ID, request);

        assertAll(
                () -> assertEquals(HttpStatus.OK,
                        response.getStatusCode(),
                        "Error: Request should be accepted"),

                () -> assertEquals(
                        List.of(false, true, false),
                        repeatedFlags,
                        "Error: Second occurrence should be marked repeated")
        );
    }

    @Test
    @DisplayName("EvaluateShots: Should normalize lowercase rows")
    void receiveShots15() {

        prepareSession(gameWithResults(
                5, miss(), miss(), miss()));

        ShotRequest request = shotRequest(
                position("a", 1),
                position("b", 2),
                position("c", 3)
        );

        ResponseEntity<?> response =
                controller.receiveShots(GAME_ID, request);

        ShotResponse body =
                assertInstanceOf(ShotResponse.class,
                        response.getBody());

        assertAll(
                () -> assertEquals("A",
                        body.results.get(0).row,
                        "Error: Row should be normalized to A"),

                () -> assertEquals("B",
                        body.results.get(1).row,
                        "Error: Row should be normalized to B"),

                () -> assertEquals("C",
                        body.results.get(2).row,
                        "Error: Row should be normalized to C"),

                () -> assertEquals(1,
                        body.results.get(0).column,
                        "Error: Column should remain 1")
        );
    }
}