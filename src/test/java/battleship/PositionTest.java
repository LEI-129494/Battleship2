
package battleship;

import org.junit.jupiter.api.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Position.
 */
public class PositionTest {

	private Position position;

	@BeforeEach
	void setUp() {
		position = new Position(2, 3);
	}

	@AfterEach
	void tearDown() {
		position = null;
	}

	@Test
	@DisplayName("Should create a position with integer coordinates")
	void constructor() {
		Position pos = new Position(1, 1);

		assertNotNull(pos);
		assertEquals(1, pos.getRow());
		assertEquals(1, pos.getColumn());
		assertFalse(pos.isOccupied());
		assertFalse(pos.isHit());
	}

	@Test
	@DisplayName("Should create a position using classic coordinates")
	void constructorWithClassicCoordinates() {
		Position pos = new Position('C', 4);

		assertEquals(2, pos.getRow());
		assertEquals(3, pos.getColumn());
		assertEquals('C', pos.getClassicRow());
		assertEquals(4, pos.getClassicColumn());
		assertFalse(pos.isOccupied());
		assertFalse(pos.isHit());
	}

	@Test
	@DisplayName("Should return the row")
	void getRow() {
		assertEquals(2, position.getRow());
	}

	@Test
	@DisplayName("Should return the column")
	void getColumn() {
		assertEquals(3, position.getColumn());
	}

	@Test
	@DisplayName("Should return the classic row")
	void getClassicRow() {
		assertEquals('C', position.getClassicRow());
	}

	@Test
	@DisplayName("Should return the classic column")
	void getClassicColumn() {
		assertEquals(4, position.getClassicColumn());
	}

	@Test
	@DisplayName("Should generate a random position inside the board")
	void randomPosition() {
		Position random = Position.randomPosition();

		assertNotNull(random);
		assertTrue(random.isInside());
	}

	@Test
	@DisplayName("Should identify a valid position at the beginning of the board")
	void isInsideAtBeginning() {
		position = new Position(0, 0);

		assertTrue(position.isInside());
	}

	@Test
	@DisplayName("Should identify a valid position at the end of the board")
	void isInsideAtEnd() {
		position = new Position(
				Game.BOARD_SIZE - 1,
				Game.BOARD_SIZE - 1
		);

		assertTrue(position.isInside());
	}

	@Test
	@DisplayName("Should reject a position with negative row")
	void isInsideWithNegativeRow() {
		position = new Position(-1, 5);

		assertFalse(position.isInside());
	}

	@Test
	@DisplayName("Should reject a position with negative column")
	void isInsideWithNegativeColumn() {
		position = new Position(5, -1);

		assertFalse(position.isInside());
	}

	@Test
	@DisplayName("Should reject a position with row outside the board")
	void isInsideWithRowOutsideBoard() {
		position = new Position(Game.BOARD_SIZE, 5);

		assertFalse(position.isInside());
	}

	@Test
	@DisplayName("Should reject a position with column outside the board")
	void isInsideWithColumnOutsideBoard() {
		position = new Position(5, Game.BOARD_SIZE);

		assertFalse(position.isInside());
	}

	@Test
	@DisplayName("Should detect horizontal adjacency")
	void isAdjacentToHorizontal() {
		Position other = new Position(2, 4);

		assertTrue(position.isAdjacentTo(other));
	}

	@Test
	@DisplayName("Should detect vertical adjacency")
	void isAdjacentToVertical() {
		Position other = new Position(3, 3);

		assertTrue(position.isAdjacentTo(other));
	}

	@Test
	@DisplayName("Should detect diagonal adjacency")
	void isAdjacentToDiagonal() {
		Position other = new Position(3, 4);

		assertTrue(position.isAdjacentTo(other));
	}

	@Test
	@DisplayName("Should detect non-adjacent positions")
	void isAdjacentToNonAdjacent() {
		Position other = new Position(4, 5);

		assertFalse(position.isAdjacentTo(other));
	}

	@Test
	@DisplayName("Should consider a position adjacent to itself")
	void isAdjacentToItself() {
		assertTrue(position.isAdjacentTo(position));
	}

	@Test
	@DisplayName("Should throw NullPointerException when checking adjacency with null")
	void isAdjacentToWithNull() {
		assertThrows(
				NullPointerException.class,
				() -> position.isAdjacentTo(null)
		);
	}

	@Test
	@DisplayName("Should return eight adjacent positions from the center")
	void adjacentPositionsFromCenter() {
		Position pos = new Position(5, 5);

		List<IPosition> adjacent = pos.adjacentPositions();

		assertEquals(8, adjacent.size());
	}

	@Test
	@DisplayName("Should return three adjacent positions from a corner")
	void adjacentPositionsFromCorner() {
		Position pos = new Position(0, 0);

		List<IPosition> adjacent = pos.adjacentPositions();

		assertEquals(3, adjacent.size());
	}

	@Test
	@DisplayName("Should return five adjacent positions from an edge")
	void adjacentPositionsFromEdge() {
		Position pos = new Position(0, 5);

		List<IPosition> adjacent = pos.adjacentPositions();

		assertEquals(5, adjacent.size());
	}

	@Test
	@DisplayName("Should return valid adjacent positions only")
	void adjacentPositionsShouldAllBeInside() {
		Position pos = new Position(0, 0);

		List<IPosition> adjacent = pos.adjacentPositions();

		for (IPosition adjacentPosition : adjacent) {
			assertTrue(adjacentPosition.isInside());
		}
	}

	@Test
	@DisplayName("Should initially not be occupied and become occupied")
	void isOccupied() {
		assertFalse(position.isOccupied());

		position.occupy();

		assertTrue(position.isOccupied());
	}

	@Test
	@DisplayName("Should initially not be hit and become hit")
	void isHit() {
		assertFalse(position.isHit());

		position.shoot();

		assertTrue(position.isHit());
	}

	@Test
	@DisplayName("Should consider positions with the same coordinates equal")
	void equalsSameCoordinates() {
		Position same = new Position(2, 3);

		assertTrue(position.equals(same));
	}

	@Test
	@DisplayName("Should not consider null equal")
	void equalsNull() {
		assertFalse(position.equals(null));
	}

	@Test
	@DisplayName("Should not consider a different object equal")
	void equalsDifferentObject() {
		Object other = new Object();

		assertFalse(position.equals(other));
	}

	@Test
	@DisplayName("Should not consider positions with different columns equal")
	void equalsDifferentColumn() {
		Position other = new Position(2, 4);

		assertFalse(position.equals(other));
	}

	@Test
	@DisplayName("Should not consider positions with different rows equal")
	void equalsDifferentRow() {
		Position other = new Position(3, 3);

		assertFalse(position.equals(other));
	}

	@Test
	@DisplayName("Should consider a position equal to itself")
	void equalsItself() {
		assertTrue(position.equals(position));
	}

	@Test
	@DisplayName("Should have equal hash codes for equal positions")
	void hashCodeConsistency() {
		Position same = new Position(2, 3);

		assertEquals(position.hashCode(), same.hashCode());
	}

	@Test
	@DisplayName("Should return the correct string representation")
	void toStringFormat() {
		assertEquals("C4", position.toString());
	}
	@Test
	@DisplayName("Should return false when columns are not adjacent")
	void isAdjacentToWithNonAdjacentColumns() {
		Position other = new Position(2, 5);

		assertFalse(position.isAdjacentTo(other));
	}
}
