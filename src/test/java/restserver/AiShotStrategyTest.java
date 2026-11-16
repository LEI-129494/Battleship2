package restserver;

import battleship.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AiShotStrategyTest {

    private AiShotStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AiShotStrategy();
    }

    @AfterEach
    void tearDown() {
        strategy = null;
    }
    //metodos auxiliares
    private IGame createGame(List<IMove> moves) {
        return (IGame) Proxy.newProxyInstance(
                IGame.class.getClassLoader(),
                new Class<?>[]{IGame.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getAlienMoves")) {
                        return moves;
                    }

                    throw new UnsupportedOperationException(
                            "Unexpected method: " + method.getName());
                }
        );
    }

    private IMove createMove(
            List<IPosition> shots,
            List<IGame.ShotResult> results) {
        return new Move(1, shots, results);
    }

    private void assertValidShots(List<IPosition> shots) {
        assertNotNull(shots,
                "Error: Shot list should not be null");

        assertEquals(new HashSet<>(shots).size(), shots.size(),
                "Error: AI should not repeat positions");

        for (IPosition shot : shots) {
            assertNotNull(shot,
                    "Error: Shot position should not be null");
        }
    }

    private IGame.ShotResult hitResult() {
        IShip ship = new Caravel(
                Compass.NORTH, new Position(4, 4));

        return new IGame.ShotResult(
                true, false, ship, false);
    }

    private List<IPosition> checkerboardPositions() {
        List<IPosition> positions = new ArrayList<>();

        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                if ((r + c) % 2 == 0) {
                    positions.add(new Position(r, c));
                }
            }
        }

        return positions;
    }

    //testes

    @Test
    @DisplayName("HUNT: Should choose checkerboard positions")
    void computeShots1() {
        IGame game = createGame(List.of());

        List<IPosition> shots = strategy.computeShots(game);

        assertValidShots(shots);

        assertEquals(Game.NUMBER_SHOTS, shots.size(),
                "Error: Incorrect number of shots");

        for (IPosition shot : shots) {
            assertEquals(0,
                    (shot.getRow() + shot.getColumn()) % 2,
                    "Error: HUNT should use checkerboard positions");
        }
    }

    @Test
    @DisplayName("HUNT: Should avoid previous shots")
    void computeShots2() {
        List<IPosition> previous = List.of(
                new Position(0, 0),
                new Position(2, 2),
                new Position(4, 4)
        );

        IGame game = createGame(List.of(
                createMove(previous, List.of())
        ));

        List<IPosition> shots = strategy.computeShots(game);

        assertValidShots(shots);

        assertEquals(Game.NUMBER_SHOTS, shots.size(),
                "Error: Incorrect number of shots");

        for (IPosition shot : shots) {
            assertFalse(previous.contains(shot),
                    "Error: AI repeated a previous shot");
        }
    }

    @Test
    @DisplayName("TARGET: Should prioritize adjacent positions")
    void computeShots3() {
        IPosition hit = new Position(4, 4);

        IGame game = createGame(List.of(
                createMove(List.of(hit), List.of(hitResult()))
        ));

        List<IPosition> shots = strategy.computeShots(game);

        assertValidShots(shots);

        assertEquals(Game.NUMBER_SHOTS, shots.size(),
                "Error: Incorrect number of shots");

        assertTrue(
                shots.stream().anyMatch(
                        hit.adjacentPositions()::contains),
                "Error: TARGET should prioritize adjacent cells");

        assertFalse(shots.contains(hit),
                "Error: AI should not repeat the hit position");
    }

    @Test
    @DisplayName("HUNT: Should return to searching after sinking")
    void computeShots4() {
        IPosition hit = new Position(4, 4);

        IShip ship = new Barge(
                Compass.NORTH, new Position(4, 4));

        IGame.ShotResult result = new IGame.ShotResult(
                true, false, ship, true);

        IGame game = createGame(List.of(
                createMove(List.of(hit), List.of(result))
        ));

        List<IPosition> shots = strategy.computeShots(game);

        assertValidShots(shots);

        assertEquals(Game.NUMBER_SHOTS, shots.size(),
                "Error: Incorrect number of shots");

        assertFalse(shots.contains(hit),
                "Error: AI should not repeat a previous shot");

        for (IPosition shot : shots) {
            assertEquals(0,
                    (shot.getRow() + shot.getColumn()) % 2,
                    "Error: AI should return to HUNT");
        }
    }

    @Test
    @DisplayName("FALLBACK: Should choose non-checkerboard cells")
    void computeShots5() {
        List<IPosition> previous = checkerboardPositions();

        IGame game = createGame(List.of(
                createMove(previous, List.of())
        ));

        List<IPosition> shots = strategy.computeShots(game);

        assertValidShots(shots);

        assertEquals(Game.NUMBER_SHOTS, shots.size(),
                "Error: Fallback should fill the available slots");

        for (IPosition shot : shots) {
            assertFalse(previous.contains(shot),
                    "Error: Fallback should not repeat positions");

            assertEquals(1,
                    (shot.getRow() + shot.getColumn()) % 2,
                    "Error: Fallback should use remaining cells");
        }
    }

    @Test
    @DisplayName("FALLBACK: Should return empty when board is exhausted")
    void computeShots6() {
        List<IPosition> previous = new ArrayList<>();

        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                previous.add(new Position(r, c));
            }
        }

        IGame game = createGame(List.of(
                createMove(previous, List.of())
        ));

        List<IPosition> shots = strategy.computeShots(game);

        assertNotNull(shots,
                "Error: Shot list should not be null");

        assertTrue(shots.isEmpty(),
                "Error: No shots should be available");
    }

    @Test
    @DisplayName("HUNT: Should ignore invalid shot results")
    void computeShots7() {
        IShip ship = new Caravel(
                Compass.NORTH, new Position(4, 4));

        List<IPosition> previous = List.of(
                new Position(0, 0),
                new Position(0, 2),
                new Position(0, 4),
                new Position(0, 6),
                new Position(0, 8),
                new Position(1, 1)
        );

        List<IGame.ShotResult> results = Arrays.asList(
                null,
                new IGame.ShotResult(
                        false, false, ship, false),
                new IGame.ShotResult(
                        true, true, ship, false),
                new IGame.ShotResult(
                        true, false, null, false),
                new IGame.ShotResult(
                        true, false, ship, true)
        );

        IGame game = createGame(List.of(
                createMove(previous, results)
        ));

        List<IPosition> shots = strategy.computeShots(game);

        assertValidShots(shots);

        assertEquals(Game.NUMBER_SHOTS, shots.size(),
                "Error: Invalid metadata should not prevent HUNT");

        for (IPosition shot : shots) {
            assertFalse(previous.contains(shot),
                    "Error: Previous positions should not be repeated");

            assertEquals(0,
                    (shot.getRow() + shot.getColumn()) % 2,
                    "Error: Invalid hits should not activate TARGET");
        }
    }

    @Test
    @DisplayName("TARGET: Should avoid shared neighbour duplicates")
    void computeShots8() {
        IPosition hit1 = new Position(4, 4);
        IPosition hit2 = new Position(4, 6);
        IPosition shared = new Position(4, 5);

        List<IPosition> blocked = new ArrayList<>(
                hit1.adjacentPositions());

        blocked.remove(shared);

        List<IMove> moves = List.of(
                createMove(List.of(hit1), List.of(hitResult())),
                createMove(List.of(hit2), List.of(hitResult())),
                createMove(blocked, List.of())
        );

        List<IPosition> shots = strategy.computeShots(
                createGame(moves));

        assertValidShots(shots);

        assertEquals(Game.NUMBER_SHOTS, shots.size(),
                "Error: TARGET should choose three shots");

        assertTrue(shots.contains(shared),
                "Error: Shared neighbour should be considered");

        assertFalse(shots.contains(hit1) || shots.contains(hit2),
                "Error: Previous hits should not be repeated");
    }

    @Test
    @DisplayName("TARGET: Should stop after reaching shot limit")
    void computeShots9() {
        IPosition hit1 = new Position(4, 4);
        IPosition hit2 = new Position(7, 7);

        IGame game = createGame(List.of(
                createMove(List.of(hit1), List.of(hitResult())),
                createMove(List.of(hit2), List.of(hitResult()))
        ));

        List<IPosition> shots = strategy.computeShots(game);

        assertValidShots(shots);

        assertEquals(Game.NUMBER_SHOTS, shots.size(),
                "Error: TARGET should respect shot limit");

        assertTrue(
                hit1.adjacentPositions().containsAll(shots),
                "Error: First target should fill the shot limit");

        assertFalse(shots.contains(hit1) || shots.contains(hit2),
                "Error: Previously hit positions must be excluded");
    }

    @Test
    @DisplayName("FALLBACK: Should skip positions selected by TARGET")
    void computeShots10() {
        IPosition hit = new Position(0, 0);

        List<IPosition> previous = checkerboardPositions();

        IGame game = createGame(List.of(
                createMove(
                        List.of(hit),
                        List.of(hitResult())),
                createMove(previous, List.of())
        ));

        List<IPosition> shots = strategy.computeShots(game);

        assertValidShots(shots);

        assertEquals(Game.NUMBER_SHOTS, shots.size(),
                "Error: TARGET and fallback should fill shot slots");

        assertTrue(shots.contains(new Position(0, 1)),
                "Error: TARGET should select the first neighbour");

        assertTrue(shots.contains(new Position(1, 0)),
                "Error: TARGET should select the second neighbour");

        for (IPosition shot : shots) {
            assertFalse(previous.contains(shot),
                    "Error: Previously fired cells must be excluded");
        }
    }

    @Test
    @DisplayName("HUNT: Should exclude positions already selected by TARGET")
    void computeShots11() {
        IPosition hit = new Position(4, 5);

        List<IPosition> neighbours =
                hit.adjacentPositions();

        IPosition available = neighbours.get(0);

        List<IPosition> blocked =
                new ArrayList<>(neighbours);

        blocked.remove(available);

        IGame game = createGame(List.of(
                createMove(List.of(hit), List.of(hitResult())),
                createMove(blocked, List.of())
        ));

        List<IPosition> shots = strategy.computeShots(game);

        assertValidShots(shots);

        assertEquals(Game.NUMBER_SHOTS, shots.size(),
                "Error: HUNT should complete TARGET's remaining slots");

        assertTrue(shots.contains(available),
                "Error: TARGET should choose the available neighbour");

        for (IPosition shot : shots) {
            assertFalse(blocked.contains(shot),
                    "Error: Blocked neighbours should not be selected");
        }
    }
}