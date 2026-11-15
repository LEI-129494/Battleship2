
package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class ShipTest {

    private Ship ship, shipBorder;

    @BeforeEach
    void setUp() {
        ship = new Barge(Compass.NORTH, new Position(5, 5));
        shipBorder = new Barge(Compass.NORTH, new Position(0, 0));
    }

    // Classe auxiliar para testar navios com várias posições
    private static class TestShip extends Ship {

        TestShip(Compass bearing, Position... positions) {
            super("Test", bearing, positions[0], positions.length);
            this.positions.addAll(Arrays.asList(positions));
        }
    }

    @Test
    void testConstructor() {
        assertNotNull(ship);
        assertEquals("Barca", ship.getCategory());
        assertEquals(Compass.NORTH, ship.getBearing());
        assertEquals(1, ship.getSize());
        assertFalse(ship.getPositions().isEmpty());
    }

    @Test
    void testGetCategory() {
        assertEquals("Barca", ship.getCategory());
    }

    @Test
    void testGetSize() {
        assertEquals(1, ship.getSize());
    }

    @Test
    void testGetBearing() {
        assertEquals(Compass.NORTH, ship.getBearing());
    }

    @Test
    void testGetPositions() {
        List<IPosition> positions = ship.getPositions();

        assertNotNull(positions);
        assertEquals(1, positions.size());
        assertEquals(5, positions.get(0).getRow());
        assertEquals(5, positions.get(0).getColumn());
    }

    @Test
    void testGetPosition() {
        assertEquals(new Position(5, 5), ship.getPosition());
    }

    @Test
    void testStillFloating1() {
        assertTrue(ship.stillFloating());
    }

    @Test
    void testStillFloating2() {
        ship.getPositions().get(0).shoot();
        assertFalse(ship.stillFloating());
    }

    @Test
    void testStillFloatingMultiplePositions() {
        Ship multiShip = new TestShip(
                Compass.NORTH,
                new Position(5, 5),
                new Position(5, 6)
        );

        multiShip.getPositions().get(0).shoot();
        assertTrue(multiShip.stillFloating());

        multiShip.getPositions().get(1).shoot();
        assertFalse(multiShip.stillFloating());
    }

    @Test
    void testShoot1() {
        Position target = new Position(5, 5);

        ship.shoot(target);

        assertTrue(ship.getPositions().get(0).isHit());
    }

    @Test
    void testShoot2() {
        Position target = new Position(0, 0);

        ship.shoot(target);

        assertFalse(ship.getPositions().get(0).isHit());
    }

    @Test
    void testShootSecondPosition() {
        Ship multiShip = new TestShip(
                Compass.NORTH,
                new Position(5, 5),
                new Position(5, 6)
        );

        multiShip.shoot(new Position(5, 6));

        assertFalse(multiShip.getPositions().get(0).isHit());
        assertTrue(multiShip.getPositions().get(1).isHit());
    }

    @Test
    void testShootMultiplePositions() {
        Ship multiShip = new TestShip(
                Compass.NORTH,
                new Position(5, 5),
                new Position(5, 6)
        );

        multiShip.shoot(new Position(5, 5));

        assertTrue(multiShip.getPositions().get(0).isHit());
        assertFalse(multiShip.getPositions().get(1).isHit());
    }

    @Test
    void testShootNoMatchingPosition() {
        Ship multiShip = new TestShip(
                Compass.NORTH,
                new Position(5, 5),
                new Position(5, 6)
        );

        multiShip.shoot(new Position(7, 7));

        assertFalse(multiShip.getPositions().get(0).isHit());
        assertFalse(multiShip.getPositions().get(1).isHit());
    }

    @Test
    void testOccupies1() {
        assertTrue(ship.occupies(new Position(5, 5)));
    }

    @Test
    void testOccupies2() {
        assertFalse(ship.occupies(new Position(1, 1)));
    }

    @Test
    void testOccupiesSecondPosition() {
        Ship multiShip = new TestShip(
                Compass.NORTH,
                new Position(5, 5),
                new Position(5, 6)
        );

        assertTrue(multiShip.occupies(new Position(5, 6)));
    }

    @Test
    void testOccupiesFirstPositionFalseThenSecondTrue() {
        Ship multiShip = new TestShip(
                Compass.NORTH,
                new Position(5, 5),
                new Position(5, 6)
        );

        assertTrue(multiShip.occupies(new Position(5, 6)));
    }

    @Test
    void testTooCloseToShip1() {
        Ship nearbyShip = new Barge(
                Compass.NORTH, new Position(5, 6)
        );

        assertTrue(ship.tooCloseTo(nearbyShip));
    }

    @Test
    void testTooCloseToShip2() {
        Ship farShip = new Barge(
                Compass.NORTH, new Position(10, 10)
        );

        assertFalse(ship.tooCloseTo(farShip));
    }

    @Test
    void testTooCloseToShipSecondPosition() {
        Ship otherShip = new TestShip(
                Compass.NORTH,
                new Position(10, 10),
                new Position(5, 6)
        );

        assertTrue(ship.tooCloseTo(otherShip));
    }

    @Test
    void testTooCloseToShipFirstPositionFalseThenSecondTrue() {
        Ship otherShip = new TestShip(
                Compass.NORTH,
                new Position(10, 10),
                new Position(5, 6)
        );

        assertTrue(ship.tooCloseTo(otherShip));
    }

    @Test
    void testTooCloseToPosition1() {
        assertTrue(ship.tooCloseTo(new Position(5, 6)));
    }

    @Test
    void testTooCloseToPosition2() {
        assertFalse(ship.tooCloseTo(new Position(7, 7)));
    }

    @Test
    void testTooCloseToSecondPosition() {
        Ship multiShip = new TestShip(
                Compass.NORTH,
                new Position(5, 5),
                new Position(10, 10)
        );

        assertTrue(multiShip.tooCloseTo(new Position(10, 11)));
    }

    @Test
    void testTooCloseToPositionSecondPositionFalseThenTrue() {
        Ship multiShip = new TestShip(
                Compass.NORTH,
                new Position(5, 5),
                new Position(10, 10)
        );

        assertTrue(multiShip.tooCloseTo(new Position(10, 11)));
    }

    @Test
    void testGetTopMostPos() {
        assertEquals(5, ship.getTopMostPos());
    }

    @Test
    void testGetBottomMostPos() {
        assertEquals(5, ship.getBottomMostPos());
    }

    @Test
    void testGetLeftMostPos() {
        assertEquals(5, ship.getLeftMostPos());
    }

    @Test
    void testGetRightMostPos() {
        assertEquals(5, ship.getRightMostPos());
    }

    @Test
    void testGetExtremePositionsMultiplePositions() {
        Ship multiShip = new TestShip(
                Compass.NORTH,
                new Position(5, 5),
                new Position(3, 7),
                new Position(8, 2),
                new Position(6, 9)
        );

        assertEquals(3, multiShip.getTopMostPos());
        assertEquals(8, multiShip.getBottomMostPos());
        assertEquals(2, multiShip.getLeftMostPos());
        assertEquals(9, multiShip.getRightMostPos());
    }

    @Test
    void testGetAdjacentPositions() {
        assertEquals(8, ship.getAdjacentPositions().size());
        assertEquals(3, shipBorder.getAdjacentPositions().size());
    }

    @Test
    void testGetAdjacentPositionsMultiplePositions() {
        Ship multiShip = new TestShip(
                Compass.NORTH,
                new Position(5, 5),
                new Position(5, 6)
        );

        List<IPosition> adjacent = multiShip.getAdjacentPositions();

        assertEquals(10, adjacent.size());
        assertFalse(adjacent.contains(new Position(5, 5)));
        assertFalse(adjacent.contains(new Position(5, 6)));
    }

    @Test
    void testSink() {
        ship.sink();

        assertFalse(ship.stillFloating());
        assertTrue(ship.getPositions().get(0).isHit());
    }

    @Test
    void testSinkMultiplePositions() {
        Ship multiShip = new TestShip(
                Compass.NORTH,
                new Position(5, 5),
                new Position(5, 6)
        );

        multiShip.sink();

        assertFalse(multiShip.stillFloating());

        for (IPosition position : multiShip.getPositions()) {
            assertTrue(position.isHit());
        }
    }

    @Test
    void testIsInsideBoard() {
        assertTrue(ship.isInsideBoard());
        assertTrue(shipBorder.isInsideBoard());
    }

    @Test
    void testIsOutsideLeft() {
        Ship outside = new TestShip(
                Compass.NORTH,
                new Position(5, -1)
        );

        assertFalse(outside.isInsideBoard());
    }

    @Test
    void testIsOutsideRight() {
        Ship outside = new TestShip(
                Compass.NORTH,
                new Position(5, Game.BOARD_SIZE)
        );

        assertFalse(outside.isInsideBoard());
    }

    @Test
    void testIsOutsideTop() {
        Ship outside = new TestShip(
                Compass.NORTH,
                new Position(-1, 5)
        );

        assertFalse(outside.isInsideBoard());
    }

    @Test
    void testIsOutsideBottom() {
        Ship outside = new TestShip(
                Compass.NORTH,
                new Position(Game.BOARD_SIZE, 5)
        );

        assertFalse(outside.isInsideBoard());
    }

    @Test
    void testBuildShipBarge() {
        Ship result = Ship.buildShip(
                "barca",
                Compass.NORTH,
                new Position(5, 5)
        );

        assertNotNull(result);
        assertEquals("Barca", result.getCategory());
    }

    @Test
    void testBuildShipCaravel() {
        assertNotNull(Ship.buildShip(
                "caravela",
                Compass.NORTH,
                new Position(5, 5)
        ));
    }

    @Test
    void testBuildShipCarrack() {
        assertNotNull(Ship.buildShip(
                "nau",
                Compass.NORTH,
                new Position(5, 5)
        ));
    }

    @Test
    void testBuildShipFrigate() {
        assertNotNull(Ship.buildShip(
                "fragata",
                Compass.NORTH,
                new Position(5, 5)
        ));
    }

    @Test
    void testBuildShipGalleon() {
        assertNotNull(Ship.buildShip(
                "galeao",
                Compass.NORTH,
                new Position(5, 5)
        ));
    }

    @Test
    void testBuildShipInvalidCategory() {
        Ship result = Ship.buildShip(
                "invalid",
                Compass.NORTH,
                new Position(5, 5)
        );

        assertNull(result);
    }

    @Test
    void testToString() {
        assertEquals("[Barca n F6]", ship.toString());
    }

    @Test
    void testGetAdjacentPositionsDoesNotAddDuplicates() {
        Position first = new Position(5, 5);
        Position second = new Position(5, 6);

        Ship multiShip = new TestShip(
                Compass.NORTH,
                first,
                second
        );

        List<IPosition> adjacent = multiShip.getAdjacentPositions();

        assertEquals(
                adjacent.size(),
                adjacent.stream().distinct().count()
        );
    }

    // Novos testes para os argumentos nulos e inválidos

    @Test
    void testBuildShipNullCategory() {
        assertThrows(AssertionError.class, () ->
                Ship.buildShip(null, Compass.NORTH, new Position(5, 5))
        );
    }

    @Test
    void testBuildShipNullBearing() {
        assertThrows(AssertionError.class, () ->
                Ship.buildShip("barca", null, new Position(5, 5))
        );
    }

    @Test
    void testBuildShipNullPosition() {
        assertThrows(AssertionError.class, () ->
                Ship.buildShip("barca", Compass.NORTH, null)
        );
    }

    @Test
    void testShootNullPosition() {
        assertThrows(AssertionError.class, () ->
                ship.shoot(null)
        );
    }

    @Test
    void testShootPositionOutsideBoard() {
        assertThrows(AssertionError.class, () ->
                ship.shoot(new Position(-1, 5))
        );
    }

    @Test
    void testOccupiesNullPosition() {
        assertThrows(AssertionError.class, () ->
                ship.occupies(null)
        );
    }

    @Test
    void testTooCloseToNullPosition() {
        assertThrows(AssertionError.class, () ->
                ship.tooCloseTo((IPosition) null)
        );
    }

    @Test
    void testTooCloseToNullShip() {
        assertThrows(AssertionError.class, () ->
                ship.tooCloseTo((IShip) null)
        );
    }
}
