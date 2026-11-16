package battleship;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    private final Path dataDir = Paths.get("data");
    private final Path dbFile = dataDir.resolve("battleship.db");

    private Path backupFile;

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(dataDir);
        assertFalse(Files.isDirectory(dbFile),
                "Erro: battleship.db não deveria ser uma diretoria");

        if (Files.exists(dbFile)) {
            backupFile = Files.createTempFile(
                    dataDir, "battleship-backup-", ".db");

            Files.move(dbFile, backupFile,
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            Files.deleteIfExists(dbFile);
        } finally {
            if (backupFile != null) {
                Files.move(backupFile, dbFile,
                        StandardCopyOption.REPLACE_EXISTING);
                backupFile = null;
            }
        }
    }

    private String captureOutput(Runnable action) {
        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PrintStream originalOut = System.out;

        try (PrintStream capture = new PrintStream(
                output, true, StandardCharsets.UTF_8)) {

            System.setOut(capture);
            action.run();

        } finally {
            System.setOut(originalOut);
        }

        return output.toString(StandardCharsets.UTF_8);
    }

    private String captureError(Runnable action) {
        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PrintStream originalErr = System.err;

        try (PrintStream capture = new PrintStream(
                output, true, StandardCharsets.UTF_8)) {

            System.setErr(capture);
            action.run();

        } finally {
            System.setErr(originalErr);
        }

        return output.toString(StandardCharsets.UTF_8);
    }

    private void simulateDatabaseError() throws Exception {
        Files.createDirectory(dbFile);
    }

    @Test
    @DisplayName("Inicialização cria a tabela shots")
    void initializeDatabase() throws Exception {

        DatabaseManager.initializeDatabase();

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:data/battleship.db");
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT name FROM sqlite_master " +
                             "WHERE type = 'table' AND name = 'shots'")) {

            assertTrue(result.next(),
                    "Erro: a tabela shots deveria existir");
        }

        // A segunda inicialização também deve funcionar.
        assertDoesNotThrow(
                DatabaseManager::initializeDatabase,
                "Erro: inicializar uma tabela existente não deveria falhar");
    }

    @Test
    @DisplayName("Inicialização trata erros de ligação SQLite")
    void initializeDatabaseException() throws Exception {

        simulateDatabaseError();

        String error = captureError(
                DatabaseManager::initializeDatabase
        );

        assertTrue(
                error.contains("Erro ao inicializar base de dados:"),
                "Erro: deveria apresentar a mensagem de falha na inicialização");
    }

    @Test
    @DisplayName("Guarda uma jogada corretamente")
    void saveShot() throws Exception {

        DatabaseManager.initializeDatabase();

        int turnNumber = 1;
        String row = "B";
        int column = 2;
        String shotResult = "HIT";

        DatabaseManager.saveShot(
                turnNumber, row, column, shotResult
        );

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:data/battleship.db");
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT turn_number, row_position, " +
                             "column_position, result FROM shots")) {

            assertTrue(result.next(),
                    "Erro: a jogada deveria estar guardada");

            assertAll(
                    () -> assertEquals(
                            turnNumber,
                            result.getInt("turn_number"),
                            "Erro: número da jogada incorreto"),

                    () -> assertEquals(
                            row,
                            result.getString("row_position"),
                            "Erro: linha incorreta"),

                    () -> assertEquals(
                            column,
                            result.getInt("column_position"),
                            "Erro: coluna incorreta"),

                    () -> assertEquals(
                            shotResult,
                            result.getString("result"),
                            "Erro: resultado incorreto")
            );

            assertFalse(result.next(),
                    "Erro: deveria existir apenas uma jogada");
        }
    }

    @Test
    @DisplayName("SaveShot trata erros de ligação SQLite")
    void saveShotException() throws Exception {

        simulateDatabaseError();

        String error = captureError(
                () -> DatabaseManager.saveShot(1, "A", 1, "HIT")
        );

        assertTrue(
                error.contains("Erro ao guardar jogada:"),
                "Erro: deveria apresentar a mensagem de falha ao guardar");
    }

    @Test
    @DisplayName("Apresenta mensagem quando o histórico está vazio")
    void printHistory1() {

        DatabaseManager.initializeDatabase();

        String output = captureOutput(
                DatabaseManager::printHistory
        );

        assertAll(
                () -> assertTrue(
                        output.contains("===== HISTÓRICO DE JOGADAS ====="),
                        "Erro: deveria apresentar o cabeçalho"),

                () -> assertTrue(
                        output.contains("Ainda não existem jogadas guardadas."),
                        "Erro: deveria indicar que o histórico está vazio")
        );
    }

    @Test
    @DisplayName("Apresenta corretamente as jogadas guardadas")
    void printHistory2() {

        DatabaseManager.initializeDatabase();

        DatabaseManager.saveShot(1, "B", 2, "HIT");
        DatabaseManager.saveShot(2, "C", 3, "MISS");

        String output = captureOutput(
                DatabaseManager::printHistory
        );

        assertAll(
                () -> assertTrue(
                        output.contains("Jogada 1 | B2 | HIT"),
                        "Erro: deveria apresentar a primeira jogada"),

                () -> assertTrue(
                        output.contains("Jogada 2 | C3 | MISS"),
                        "Erro: deveria apresentar a segunda jogada"),

                () -> assertFalse(
                        output.contains("Ainda não existem jogadas guardadas."),
                        "Erro: não deveria indicar histórico vazio")
        );
    }

    @Test
    @DisplayName("PrintHistory trata erros de ligação SQLite")
    void printHistoryException() throws Exception {

        simulateDatabaseError();

        String error = captureError(
                DatabaseManager::printHistory
        );

        assertTrue(
                error.contains("Erro ao consultar histórico:"),
                "Erro: deveria apresentar a mensagem de falha na consulta");
    }
}