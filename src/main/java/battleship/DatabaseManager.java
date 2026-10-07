package battleship;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DATABASE_URL =
            "jdbc:sqlite:data/battleship.db";

    public static void initializeDatabase() {

        String sql = """
                    CREATE TABLE IF NOT EXISTS shots (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    turn_number INTEGER NOT NULL,
                    row_position TEXT NOT NULL,
                    column_position INTEGER NOT NULL,
                    result TEXT NOT NULL
                )
                """;

        try (Connection connection =
                     DriverManager.getConnection(DATABASE_URL);
             Statement statement =
                     connection.createStatement()) {

            statement.execute(sql);

            System.out.println("Base de dados inicializada.");

        } catch (SQLException e) {
            System.err.println(
                    "Erro ao inicializar base de dados: "
                            + e.getMessage()
            );
        }
    }

    public static void saveShot(
            int turnNumber,
            String row,
            int column,
            String result) {

        String sql = """
            INSERT INTO shots
            (turn_number, row_position, column_position, result)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection connection =
                     DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, turnNumber);
            statement.setString(2, row);
            statement.setInt(3, column);
            statement.setString(4, result);

            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println(
                    "Erro ao guardar jogada: " + e.getMessage()
            );
        }
    }
    public static void printHistory() {

        String sql = """
            SELECT turn_number, row_position, column_position, result
            FROM shots
            ORDER BY turn_number, id
            """;

        try (Connection connection =
                     DriverManager.getConnection(DATABASE_URL);
             Statement statement =
                     connection.createStatement();
             ResultSet resultSet =
                     statement.executeQuery(sql)) {

            boolean hasHistory = false;

            System.out.println();
            System.out.println("===== HISTÓRICO DE JOGADAS =====");

            while (resultSet.next()) {

                hasHistory = true;

                int turnNumber = resultSet.getInt("turn_number");
                String row = resultSet.getString("row_position");
                int column = resultSet.getInt("column_position");
                String result = resultSet.getString("result");

                System.out.println(
                        "Jogada " + turnNumber +
                                " | " + row + column +
                                " | " + result
                );
            }

            if (!hasHistory) {
                System.out.println("Ainda não existem jogadas guardadas.");
            }

            System.out.println("===============================");
            System.out.println();

        } catch (SQLException e) {
            System.err.println(
                    "Erro ao consultar histórico: " + e.getMessage()
            );
        }
    }
}
