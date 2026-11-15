
package battleship;

import java.util.List;

/**
 * The interface Ship.
 */
public interface IShip {

	String getCategory();

	Integer getSize();

	List<IPosition> getPositions();

	List<IPosition> getAdjacentPositions();

	IPosition getPosition();

	Compass getBearing();

	boolean stillFloating();

	int getTopMostPos();

	int getBottomMostPos();

	int getLeftMostPos();

	int getRightMostPos();

	boolean occupies(IPosition pos);

	boolean tooCloseTo(IShip other);

	boolean tooCloseTo(IPosition pos);

	boolean isInsideBoard();

	void shoot(IPosition pos);

	void sink();
}
