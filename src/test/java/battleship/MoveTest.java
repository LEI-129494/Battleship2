
package battleship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    private static final int MOVE_NUMBER = 1;

    private Barge barge;

    @BeforeEach
    void setUp() throws Exception {

        barge = new Barge(
                Compass.NORTH,
                new Position(0, 0)
        );

        // Inicializar a base de dados
        DatabaseManager.initializeDatabase();

        // Limpar os disparos anteriores
        try (Connection connection =
                     DriverManager.getConnection(
                             "jdbc:sqlite:data/battleship.db");
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("DELETE FROM shots");
        }
    }

    // --------------------------------------------------
    // MÉTODOS AUXILIARES
    // --------------------------------------------------

    private Move createMove(IGame.ShotResult firstResult) {

        List<IPosition> shots = new ArrayList<>();
        List<IGame.ShotResult> results = new ArrayList<>();

        for (int i = 0; i < Game.NUMBER_SHOTS; i++) {

            shots.add(new Position(
                    0,
                    i % Game.BOARD_SIZE
            ));

            results.add(
                    new IGame.ShotResult(
                            true, false, null, false
                    )
            );
        }

        results.set(0, firstResult);

        return new Move(MOVE_NUMBER, shots, results);
    }

    private JsonNode process(Move move) throws Exception {

        String json = move.processEnemyFire(false);

        return new ObjectMapper().readTree(json);
    }

    private int getDatabaseShotCount() throws Exception {

        try (Connection connection =
                     DriverManager.getConnection(
                             "jdbc:sqlite:data/battleship.db");
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT COUNT(*) FROM shots")) {

            rs.next();
            return rs.getInt(1);
        }
    }

    private String getFirstDatabaseResult() throws Exception {

        try (Connection connection =
                     DriverManager.getConnection(
                             "jdbc:sqlite:data/battleship.db");
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT result FROM shots ORDER BY id LIMIT 1")) {

            assertTrue(rs.next());

            return rs.getString("result");
        }
    }

    // --------------------------------------------------
    // CONSTRUTOR E GETTERS
    // --------------------------------------------------

    @Test
    void testConstructorAndGetters() {

        List<IPosition> shots = new ArrayList<>();
        shots.add(new Position(0, 0));

        List<IGame.ShotResult> results = new ArrayList<>();

        Move move = new Move(5, shots, results);

        assertEquals(5, move.getNumber());
        assertEquals(shots, move.getShots());
        assertEquals(results, move.getShotResults());
    }

    @Test
    void testToString() {

        List<IPosition> shots = new ArrayList<>();
        shots.add(new Position(0, 0));

        List<IGame.ShotResult> results = new ArrayList<>();

        Move move = new Move(3, shots, results);

        assertEquals(
                "Move{number=3, shots=1, results=0}",
                move.toString()
        );
    }

    // --------------------------------------------------
    // PROCESS ENEMY FIRE - MISS
    // --------------------------------------------------

    @Test
    void testProcessEnemyFireMiss() throws Exception {

        Move move = createMove(
                new IGame.ShotResult(
                        true, false, null, false
                )
        );

        JsonNode json = process(move);

        assertEquals(
                Game.NUMBER_SHOTS,
                json.get("validShots").asInt()
        );

        assertEquals(
                Game.NUMBER_SHOTS,
                json.get("missedShots").asInt()
        );

        assertEquals(
                0,
                json.get("repeatedShots").asInt()
        );

        assertEquals(
                "MISS",
                getFirstDatabaseResult()
        );

        assertEquals(
                Game.NUMBER_SHOTS,
                getDatabaseShotCount()
        );
    }

    // --------------------------------------------------
    // PROCESS ENEMY FIRE - OUTSIDE
    // --------------------------------------------------

    @Test
    void testProcessEnemyFireOutside() throws Exception {

        List<IPosition> shots = new ArrayList<>();
        List<IGame.ShotResult> results = new ArrayList<>();

        for (int i = 0; i < Game.NUMBER_SHOTS; i++) {

            shots.add(new Position(0, i % Game.BOARD_SIZE));

            results.add(
                    new IGame.ShotResult(
                            true, false, null, false
                    )
            );
        }

        shots.set(0, new Position(-1, 0));

        results.set(0,
                new IGame.ShotResult(
                        false, false, null, false
                )
        );

        Move move = new Move(MOVE_NUMBER, shots, results);

        JsonNode json = process(move);

        assertEquals(
                1,
                json.get("outsideShots").asInt()
        );

        assertEquals(
                "OUTSIDE",
                getFirstDatabaseResult()
        );
    }

    // --------------------------------------------------
    // PROCESS ENEMY FIRE - REPEATED
    // --------------------------------------------------

    @Test
    void testProcessEnemyFireRepeated() throws Exception {

        Move move = createMove(
                new IGame.ShotResult(
                        true, true, null, false
                )
        );

        JsonNode json = process(move);

        assertEquals(
                1,
                json.get("repeatedShots").asInt()
        );

        assertEquals(
                "REPEATED",
                getFirstDatabaseResult()
        );
    }

    // --------------------------------------------------
    // PROCESS ENEMY FIRE - HIT
    // --------------------------------------------------

    @Test
    void testProcessEnemyFireHit() throws Exception {

        Move move = createMove(
                new IGame.ShotResult(
                        true, false, barge, false
                )
        );

        JsonNode json = process(move);

        assertEquals(
                1,
                json.get("hitsOnBoats").size()
        );

        assertEquals(
                "HIT",
                getFirstDatabaseResult()
        );

        assertEquals(
                0,
                json.get("sunkBoats").size()
        );
    }

    // --------------------------------------------------
    // PROCESS ENEMY FIRE - SUNK
    // --------------------------------------------------

    @Test
    void testProcessEnemyFireSunk() throws Exception {

        Move move = createMove(
                new IGame.ShotResult(
                        true, false, barge, true
                )
        );

        JsonNode json = process(move);

        assertEquals(
                1,
                json.get("sunkBoats").size()
        );

        assertEquals(
                "SUNK",
                getFirstDatabaseResult()
        );

        assertEquals(
                0,
                json.get("hitsOnBoats").size()
        );
    }

    // --------------------------------------------------
    // VERBOSE - ONLY REPEATED SHOTS
    // --------------------------------------------------

    @Test
    void testProcessEnemyFireVerboseWithOnlyRepeatedShots()
            throws Exception {

        List<IPosition> shots = new ArrayList<>();
        List<IGame.ShotResult> results = new ArrayList<>();

        for (int i = 0; i < Game.NUMBER_SHOTS; i++) {

            shots.add(new Position(0, i % Game.BOARD_SIZE));

            results.add(
                    new IGame.ShotResult(
                            true, true, null, false
                    )
            );
        }

        Move move = new Move(MOVE_NUMBER, shots, results);

        String json = assertDoesNotThrow(
                () -> move.processEnemyFire(true)
        );

        JsonNode response = new ObjectMapper().readTree(json);

        assertEquals(
                0,
                response.get("validShots").asInt()
        );

        assertEquals(
                Game.NUMBER_SHOTS,
                response.get("repeatedShots").asInt()
        );
    }

    // --------------------------------------------------
    // VERBOSE - HIT, REPEATED AND OUTSIDE
    // --------------------------------------------------

    @Test
    void testProcessEnemyFireVerboseWithHitAndMiss()
            throws Exception {

        assertTrue(Game.NUMBER_SHOTS >= 3,
                "Este teste necessita de pelo menos 3 disparos.");

        List<IPosition> shots = new ArrayList<>();
        List<IGame.ShotResult> results = new ArrayList<>();

        for (int i = 0; i < Game.NUMBER_SHOTS; i++) {

            shots.add(new Position(0, i % Game.BOARD_SIZE));

            results.add(
                    new IGame.ShotResult(
                            true, false, null, false
                    )
            );
        }

        // Primeiro disparo: navio atingido
        results.set(0,
                new IGame.ShotResult(
                        true, false, barge, false
                )
        );

        // Segundo disparo: repetido
        results.set(1,
                new IGame.ShotResult(
                        true, true, null, false
                )
        );

        // Terceiro disparo: fora do tabuleiro
        results.set(2,
                new IGame.ShotResult(
                        false, false, null, false
                )
        );

        Move move = new Move(MOVE_NUMBER, shots, results);

        String json = assertDoesNotThrow(
                () -> move.processEnemyFire(true)
        );

        JsonNode response = new ObjectMapper().readTree(json);

        assertEquals(
                Game.NUMBER_SHOTS - 2,
                response.get("validShots").asInt()
        );

        assertEquals(
                1,
                response.get("repeatedShots").asInt()
        );

        assertEquals(
                1,
                response.get("outsideShots").asInt()
        );

        assertEquals(
                1,
                response.get("hitsOnBoats").size()
        );

        assertEquals(
                0,
                response.get("sunkBoats").size()
        );
    }

    // --------------------------------------------------
    // VERBOSE - SUNK SHIP
    // --------------------------------------------------

    @Test
    void testProcessEnemyFireVerboseWithSunkShip()
            throws Exception {

        Move move = createMove(
                new IGame.ShotResult(
                        true, false, barge, true
                )
        );

        String json = assertDoesNotThrow(
                () -> move.processEnemyFire(true)
        );

        JsonNode response = new ObjectMapper().readTree(json);

        assertEquals(
                1,
                response.get("sunkBoats").size()
        );

        assertEquals(
                0,
                response.get("hitsOnBoats").size()
        );
    }

    // --------------------------------------------------
    // VERBOSE - ONLY MISSES
    // --------------------------------------------------

    @Test
    void testProcessEnemyFireVerboseWithOnlyMisses()
            throws Exception {

        Move move = createMove(
                new IGame.ShotResult(
                        true, false, null, false
                )
        );

        String json = assertDoesNotThrow(
                () -> move.processEnemyFire(true)
        );

        JsonNode response = new ObjectMapper().readTree(json);

        assertEquals(
                Game.NUMBER_SHOTS,
                response.get("missedShots").asInt()
        );

        assertEquals(
                0,
                response.get("sunkBoats").size()
        );

        assertEquals(
                0,
                response.get("hitsOnBoats").size()
        );
    }
}
