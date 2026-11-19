
package battleship;

import java.util.Map;

public record MoveStatistics(
        int validShots,
        int repeatedShots,
        int missedShots,
        int outsideShots,
        Map<String, Integer> sunkBoatsCount,
        Map<String, Integer> hitsPerBoat
) {}
