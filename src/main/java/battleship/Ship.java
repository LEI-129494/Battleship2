
package battleship;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * The type Ship.
 */
public abstract class Ship implements IShip {

	private static final String GALEAO = "galeao";
	private static final String FRAGATA = "fragata";
	private static final String NAU = "nau";
	private static final String CARAVELA = "caravela";
	private static final String BARCA = "barca";

	private String category;
	private Compass bearing;
	private IPosition pos;
	private Integer size;

	protected List<IPosition> positions;

	/**
	 * Create a new ship.
	 *
	 * @param shipKind the ship kind
	 * @param bearing the bearing
	 * @param pos the position
	 * @return a ship
	 */
	static Ship buildShip(String shipKind, Compass bearing, Position pos) {
		assert shipKind != null;
		assert bearing != null;
		assert pos != null;

		Ship ship;

		switch (shipKind) {
			case BARCA:
				ship = new Barge(bearing, pos);
				break;
			case CARAVELA:
				ship = new Caravel(bearing, pos);
				break;
			case NAU:
				ship = new Carrack(bearing, pos);
				break;
			case FRAGATA:
				ship = new Frigate(bearing, pos);
				break;
			case GALEAO:
				ship = new Galleon(bearing, pos);
				break;
			default:
				ship = null;
		}

		return ship;
	}

	/**
	 * Create ships.
	 *
	 * @param category the category
	 * @param bearing the bearing
	 * @param pos the position
	 * @param size the number of positions occupied
	 */
	public Ship(String category, Compass bearing, IPosition pos, int size) {
		this.category = Objects.requireNonNull(
				category, "Ship's category must not be null");
		this.bearing = Objects.requireNonNull(
				bearing, "Ship's bearing must not be null");
		this.pos = Objects.requireNonNull(
				pos, "Ship's position must not be null");

		this.size = size;
		this.positions = new ArrayList<>();
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public List<IPosition> getPositions() {
		return positions;
	}

	@Override
	public List<IPosition> getAdjacentPositions() {
		List<IPosition> adjacentPositions = new ArrayList<>();

		for (IPosition position : getPositions()) {
			List<IPosition> adjacents = position.adjacentPositions();

			for (IPosition adj : adjacents) {
				addAdjacentPositionIfNeeded(adj, adjacentPositions);
			}
		}

		return adjacentPositions;
	}

	private void addAdjacentPositionIfNeeded(IPosition adj, List<IPosition> adjacentPositions) {
		if (!getPositions().contains(adj)
				&& !adjacentPositions.contains(adj)) {
			adjacentPositions.add(adj);
		}
	}

	@Override
	public IPosition getPosition() {
		return pos;
	}

	@Override
	public Compass getBearing() {
		return bearing;
	}

	@Override
	public Integer getSize() {
		return size;
	}

	@Override
	public boolean stillFloating() {
		for (int i = 0; i < getSize(); i++) {
			if (!getPositions().get(i).isHit()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int getTopMostPos() {
		int top = getPositions().get(0).getRow();

		for (int i = 1; i < getSize(); i++) {
			if (getPositions().get(i).getRow() < top) {
				top = getPositions().get(i).getRow();
			}
		}

		return top;
	}

	@Override
	public int getBottomMostPos() {
		int bottom = getPositions().get(0).getRow();

		for (int i = 1; i < getSize(); i++) {
			if (getPositions().get(i).getRow() > bottom) {
				bottom = getPositions().get(i).getRow();
			}
		}

		return bottom;
	}

	@Override
	public int getLeftMostPos() {
		int left = getPositions().get(0).getColumn();

		for (int i = 1; i < getSize(); i++) {
			if (getPositions().get(i).getColumn() < left) {
				left = getPositions().get(i).getColumn();
			}
		}

		return left;
	}

	@Override
	public int getRightMostPos() {
		int right = getPositions().get(0).getColumn();

		for (int i = 1; i < getSize(); i++) {
			if (getPositions().get(i).getColumn() > right) {
				right = getPositions().get(i).getColumn();
			}
		}

		return right;
	}

	@Override
	public boolean occupies(IPosition pos) {
		assert pos != null;

		for (int i = 0; i < getSize(); i++) {
			if (getPositions().get(i).equals(pos)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean tooCloseTo(IShip other) {
		assert other != null;

		Iterator<IPosition> otherPos = other.getPositions().iterator();

		while (otherPos.hasNext()) {
			if (tooCloseTo(otherPos.next())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean tooCloseTo(IPosition pos) {
		assert pos != null;

		for (int i = 0; i < this.getSize(); i++) {
			if (getPositions().get(i).isAdjacentTo(pos)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isInsideBoard() {
		return getLeftMostPos() >= 0
				&& getRightMostPos() <= Game.BOARD_SIZE - 1
				&& getTopMostPos() >= 0
				&& getBottomMostPos() <= Game.BOARD_SIZE - 1;
	}

	@Override
	public void shoot(IPosition pos) {
		assert pos != null;
		assert pos.isInside();

		for (IPosition position : getPositions()) {
			if (position.equals(pos)) {
				position.shoot();
			}
		}
	}

	@Override
	public void sink() {
		for (IPosition position : getPositions()) {
			position.shoot();
		}
	}

	@Override
	public String toString() {
		return "[" + category + " " + bearing + " " + pos + "]";
	}
}
