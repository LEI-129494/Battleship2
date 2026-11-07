package battleship;

import java.util.List;

public class BoardPrinter {

    private static final char EMPTY_MARKER = '.';
    private static final char SHIP_MARKER = '#';
    private static final char SHOT_SHIP_MARKER = '*';
    private static final char SHOT_WATER_MARKER = 'o';
    private static final char SHIP_ADJACENT_MARKER = '-';

    public static void printBoard(
            IFleet fleet,
            List<IMove> moves,
            boolean show_shots,
            boolean showLegend) {

        assert fleet != null;
        assert moves != null;

        char[][] map = new char[Game.BOARD_SIZE][Game.BOARD_SIZE];

        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                map[r][c] = EMPTY_MARKER;
            }
        }

        printShips(fleet, map);

        if (show_shots) {
            printBoardShots(moves, map);
        }

        printBoardFrame(map);

        if (showLegend) {
            System.out.println(Messages.get("board.legend.title"));
            System.out.println(Messages.format(
                    "board.legend.symbols",
                    SHIP_MARKER,
                    SHIP_ADJACENT_MARKER,
                    EMPTY_MARKER));
            System.out.println(Messages.format(
                    "board.legend.shots",
                    SHOT_SHIP_MARKER,
                    SHOT_WATER_MARKER));
        }

        System.out.println();
    }

    private static void printShips(IFleet fleet, char[][] map) {
        for (IShip ship : fleet.getShips()) {
            for (IPosition shipPos : ship.getPositions()) {
                map[shipPos.getRow()][shipPos.getColumn()] = SHIP_MARKER;
            }

            if (!ship.stillFloating()) {
                for (IPosition adjacentPos : ship.getAdjacentPositions()) {
                    map[adjacentPos.getRow()][adjacentPos.getColumn()]
                            = SHIP_ADJACENT_MARKER;
                }
            }
        }
    }

    private static void printBoardShots(List<IMove> moves, char[][] map) {
        for (IMove move : moves) {
            for (IPosition shot : move.getShots()) {
                if (shot.isInside()) {
                    int row = shot.getRow();
                    int col = shot.getColumn();

                    if (map[row][col] == SHIP_MARKER) {
                        map[row][col] = SHOT_SHIP_MARKER;
                    }

                    if (map[row][col] == EMPTY_MARKER
                            || map[row][col] == SHIP_ADJACENT_MARKER) {
                        map[row][col] = SHOT_WATER_MARKER;
                    }
                }
            }
        }
    }

    private static void printBoardFrame(char[][] map) {
        System.out.println();
        System.out.print("    ");

        for (int col = 0; col < Game.BOARD_SIZE; col++) {
            System.out.print(" " + (col + 1));
        }

        System.out.println();
        System.out.print("   +-");

        for (int col = 0; col < Game.BOARD_SIZE; col++) {
            System.out.print("--");
        }

        System.out.println("+");

        for (int row = 0; row < Game.BOARD_SIZE; row++) {
            Position pos = new Position(row, 0);
            char rowLabel = pos.getClassicRow();

            System.out.print(" " + rowLabel + " |");

            for (int col = 0; col < Game.BOARD_SIZE; col++) {
                System.out.print(" " + map[row][col]);
            }

            System.out.println(" |");
        }

        System.out.print("   +");

        for (int col = 0; col < Game.BOARD_SIZE; col++) {
            System.out.print("--");
        }

        System.out.println("-+");
    }
}