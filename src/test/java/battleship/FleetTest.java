package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

class FleetTest {

	private Fleet fleet;

	@BeforeEach
	void setUp() {
		fleet = new Fleet();
	}

	@AfterEach
	void tearDown() {
		fleet = null;
	}

	private String captureOutput(Runnable action) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
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

	@Test
	@DisplayName("Constructor: Should initialize an empty fleet")
	void testConstructor() {
		assertAll(
				() -> assertNotNull(fleet,
						"Error: Fleet should not be null"),
				() -> assertTrue(fleet.getShips().isEmpty(),
						"Error: Fleet should initially be empty")
		);
	}

	@Test
	@DisplayName("CreateRandom: Should generate a full fleet")
	void testCreateRandom() {
		IFleet randomFleet = Fleet.createRandom();

		assertAll(
				() -> assertNotNull(randomFleet,
						"Error: Random fleet should not be null"),
				() -> assertEquals(Fleet.FLEET_SIZE,
						randomFleet.getShips().size(),
						"Error: Random fleet should contain the expected number of ships")
		);
	}

	@Test
	@DisplayName("AddShip: Should add a valid ship")
	void testAddShip1() {
		IShip ship = new Barge(
				Compass.NORTH, new Position(1, 1));

		assertTrue(fleet.addShip(ship),
				"Error: Valid ship should be added");

		assertEquals(1, fleet.getShips().size(),
				"Error: Fleet should contain one ship");
	}

	@Test
	@DisplayName("AddShip: Should reject a ship when fleet exceeds capacity")
	void testAddShip2() {

		for (int i = 0; i <= Fleet.FLEET_SIZE; i++) {
			fleet.getShips().add(
					new Barge(
							Compass.NORTH,
							new Position(0, 0)));
		}

		int originalSize = fleet.getShips().size();

		IShip extraShip = new Barge(
				Compass.NORTH, new Position(5, 5));

		assertAll(
				() -> assertFalse(fleet.addShip(extraShip),
						"Error: Ship should be rejected when capacity is exceeded"),

				() -> assertEquals(originalSize,
						fleet.getShips().size(),
						"Error: Fleet size should not increase")
		);
	}

	@Test
	@DisplayName("AddShip: Should reject ships outside the board")
	void testAddShip3() {
		IShip ship = new Barge(
				Compass.NORTH, new Position(99, 99));

		assertFalse(fleet.addShip(ship),
				"Error: Ship outside the board should be rejected");
	}

	@Test
	@DisplayName("AddShip: Should reject overlapping ships")
	void testAddShip4() {
		IShip ship1 = new Barge(
				Compass.NORTH, new Position(1, 1));

		IShip ship2 = new Barge(
				Compass.NORTH, new Position(1, 1));

		assertTrue(fleet.addShip(ship1),
				"Error: First ship should be added");

		assertFalse(fleet.addShip(ship2),
				"Error: Overlapping ship should be rejected");
	}

	@Test
	@DisplayName("AddShip: Should reject null")
	void testAddShipNull() {
		assertThrows(AssertionError.class,
				() -> fleet.addShip(null),
				"Error: Null ship should throw AssertionError");
	}

	@Test
	@DisplayName("GetShips: Should return the fleet ships")
	void testGetShips() {

		assertTrue(fleet.getShips().isEmpty(),
				"Error: Fleet should initially be empty");

		IShip ship = new Barge(
				Compass.NORTH, new Position(1, 1));

		fleet.addShip(ship);

		assertAll(
				() -> assertEquals(1,
						fleet.getShips().size(),
						"Error: Fleet should contain one ship"),

				() -> assertEquals(ship,
						fleet.getShips().get(0),
						"Error: Returned ship should match")
		);
	}

	@Test
	@DisplayName("GetShipsLike: Should filter by category")
	void testGetShipsLike() {
		IShip barge = new Barge(
				Compass.NORTH, new Position(1, 1));

		IShip caravel = new Caravel(
				Compass.NORTH, new Position(4, 4));

		fleet.addShip(barge);
		fleet.addShip(caravel);

		List<IShip> barges = fleet.getShipsLike("Barca");

		assertAll(
				() -> assertEquals(1, barges.size(),
						"Error: One Barge should be returned"),

				() -> assertEquals(barge, barges.get(0),
						"Error: Returned ship should be the Barge"),

				() -> assertTrue(
						fleet.getShipsLike("Galeao").isEmpty(),
						"Error: Unknown fleet category should return an empty list")
		);
	}

	@Test
	@DisplayName("GetShipsLike: Should reject null category")
	void testGetShipsLikeNull() {
		assertThrows(AssertionError.class,
				() -> fleet.getShipsLike(null),
				"Error: Null category should throw AssertionError");
	}

	@Test
	@DisplayName("GetFloatingShips: Should return floating ships")
	void testGetFloatingShips() {
		IShip barge = new Barge(
				Compass.NORTH, new Position(1, 1));

		IShip caravel = new Caravel(
				Compass.NORTH, new Position(4, 4));

		fleet.addShip(barge);
		fleet.addShip(caravel);

		assertEquals(2, fleet.getFloatingShips().size(),
				"Error: Both ships should initially be floating");

		barge.getPositions().get(0).shoot();

		List<IShip> floating = fleet.getFloatingShips();

		assertAll(
				() -> assertEquals(1, floating.size(),
						"Error: Only one ship should still be floating"),

				() -> assertEquals(caravel, floating.get(0),
						"Error: Caravel should be the remaining floating ship")
		);
	}

	@Test
	@DisplayName("GetSunkShips: Should return sunk ships")
	void testGetSunkShips() {
		IShip barge = new Barge(
				Compass.NORTH, new Position(1, 1));

		IShip caravel = new Caravel(
				Compass.NORTH, new Position(4, 4));

		fleet.addShip(barge);
		fleet.addShip(caravel);

		assertTrue(fleet.getSunkShips().isEmpty(),
				"Error: No ships should initially be sunk");

		barge.getPositions().get(0).shoot();

		List<IShip> sunk = fleet.getSunkShips();

		assertAll(
				() -> assertEquals(1, sunk.size(),
						"Error: Only one ship should be sunk"),

				() -> assertEquals(barge, sunk.get(0),
						"Error: Barge should be the sunk ship")
		);
	}

	@Test
	@DisplayName("ShipAt: Should find ships by position")
	void testShipAt() {
		IShip ship = new Barge(
				Compass.NORTH, new Position(1, 1));

		fleet.addShip(ship);

		assertAll(
				() -> assertEquals(ship,
						fleet.shipAt(new Position(1, 1)),
						"Error: Ship should be found at its position"),

				() -> assertNull(
						fleet.shipAt(new Position(5, 5)),
						"Error: Empty position should return null")
		);
	}

	@Test
	@DisplayName("ShipAt: Should reject null position")
	void testShipAtNull() {
		assertThrows(AssertionError.class,
				() -> fleet.shipAt(null),
				"Error: Null position should throw AssertionError");
	}

	@Test
	@DisplayName("ColisionRisk: Should identify collisions")
	void testColisionRisk() throws Exception {
		var method = Fleet.class.getDeclaredMethod(
				"colisionRisk", IShip.class);

		method.setAccessible(true);

		IShip ship1 = new Barge(
				Compass.NORTH, new Position(1, 1));

		IShip ship2 = new Barge(
				Compass.NORTH, new Position(1, 1));

		fleet.addShip(ship1);

		assertAll(
				() -> assertTrue(
						(Boolean) method.invoke(fleet, ship2),
						"Error: Overlapping ships should collide"),

				() -> assertFalse(
						(Boolean) method.invoke(
								fleet,
								new Barge(
										Compass.NORTH,
										new Position(5, 5))),
						"Error: Separated ships should not collide")
		);

		assertThrows(
				java.lang.reflect.InvocationTargetException.class,
				() -> method.invoke(fleet, (IShip) null),
				"Error: Null ship should cause an invocation exception");
	}

	@Test
	@DisplayName("PrintStatus: Should print fleet status")
	void testPrintStatus() {
		fleet.addShip(new Barge(
				Compass.NORTH, new Position(1, 1)));

		String output = captureOutput(fleet::printStatus);

		assertTrue(output.contains("Estado da Frota"),
				"Error: Fleet status message should be printed");
	}

	@Test
	@DisplayName("PrintMethods: Should print ship lists")
	void testPrintMethods() {
		IShip barge = new Barge(
				Compass.NORTH, new Position(1, 1));

		IShip caravel = new Caravel(
				Compass.NORTH, new Position(4, 4));

		assertTrue(fleet.addShip(barge),
				"Error: Barge should be added");

		assertTrue(fleet.addShip(caravel),
				"Error: Caravel should be added");

		// Afunda todas as posições da Caravela.
		for (IPosition position : caravel.getPositions()) {
			position.shoot();
		}

		assertFalse(caravel.stillFloating(),
				"Error: Caravel should be sunk");

		String all = captureOutput(fleet::printAllShips);

		assertAll(
				() -> assertTrue(all.contains("Barca"),
						"Error: All ships should include Barge"),

				() -> assertTrue(all.contains("Caravela"),
						"Error: All ships should include Caravel")
		);

		String floating = captureOutput(fleet::printFloatingShips);

		assertAll(
				() -> assertTrue(floating.contains("Barca"),
						"Error: Floating Barge should be printed"),

				() -> assertFalse(floating.contains("Caravela"),
						"Error: Sunk Caravel should not be printed")
		);

		String category = captureOutput(
				() -> fleet.printShipsByCategory("Barca"));

		assertAll(
				() -> assertTrue(category.contains("Barca"),
						"Error: Barge category should be printed"),

				() -> assertFalse(category.contains("Caravela"),
						"Error: Caravel should not be printed")
		);

		String direct = captureOutput(
				() -> fleet.printShips(fleet.getShips()));

		assertAll(
				() -> assertTrue(direct.contains("Barca"),
						"Error: Barge should be printed"),

				() -> assertTrue(direct.contains("Caravela"),
						"Error: Caravel should be printed")
		);

		assertThrows(AssertionError.class,
				() -> fleet.printShips(null),
				"Error: Null list should throw AssertionError");
	}

	@Test
	@DisplayName("PrintShipsByCategory: Should reject null")
	void testPrintShipsByCategoryNull() {
		assertThrows(AssertionError.class,
				() -> fleet.printShipsByCategory(null),
				"Error: Null category should throw AssertionError");
	}
}