package battleship;

import java.util.*;

public class Game implements IGame
{

	//------------------------------------------------------------------
	public static final int BOARD_SIZE = 10;
	public static final int NUMBER_SHOTS = 3;

	//------------------------------------------------------------------
	private final IFleet myFleet;
	private final List<IMove> alienMoves;

	private final IFleet alienFleet;
	private final List<IMove> myMoves;

	private Integer countInvalidShots;
	private Integer countRepeatedShots;
	private Integer countHits;
	private Integer countSinks;
	private int moveNumber;

	//------------------------------------------------------------------
	public Game(IFleet myFleet)
	{
		this.moveNumber = 1;

		this.alienMoves = new ArrayList<IMove>();
		this.myMoves = new ArrayList<IMove>();

		this.alienFleet = new Fleet();
		this.myFleet = myFleet;

		this.countInvalidShots = 0;
		this.countRepeatedShots = 0;
		this.countHits = 0;
		this.countSinks = 0;
	}

	@Override
	public IFleet getMyFleet()
	{
		return myFleet;
	}

	@Override
	public IFleet getAlienFleet()
	{
		return alienFleet;
	}

	@Override
	public List<IMove> getMyMoves()
	{
		return myMoves;
	}

	@Override
	public List<IMove> getAlienMoves()
	{
		return alienMoves;
	}

	/**
	 * Simulates a random firing action by the enemy, generating a set of unique shot coordinates
	 * and serializing them into a JSON string. The method ensures that the random shots are valid
	 * and do not duplicate existing shots in the game or previous enemy moves. After generating
	 * the shots, it applies the firing logic and serializes the result for further processing.
	 *
	 * @return A JSON string representing the list of randomly generated enemy shots.
	 * @throws RuntimeException if there is an error during the JSON serialization of the shots.
	 */
	public String randomEnemyFire() {

		Random random = new Random(System.currentTimeMillis());

		Set<IPosition> usablePositions = new HashSet<IPosition>();
		for (int r = 0; r < BOARD_SIZE; r++)
			for (int c = 0; c < BOARD_SIZE; c++)
				usablePositions.add(new Position(r, c));

		this.myFleet.getSunkShips().forEach(ship -> usablePositions.removeAll(ship.getAdjacentPositions()));
		this.alienMoves.forEach(move ->  usablePositions.removeAll(move.getShots()));

		List<IPosition> candidateShots = new ArrayList<>(usablePositions);

		List<IPosition> shots = new ArrayList<IPosition>();

		System.out.println();

		IPosition newShot = null;
		if (candidateShots.size() >= Game.NUMBER_SHOTS)
			while (shots.size() < Game.NUMBER_SHOTS) {
				newShot = candidateShots.get(random.nextInt(candidateShots.size()));
				if (!shots.contains(newShot))
					shots.add(newShot);
			}
		else {
			while (shots.size() < candidateShots.size()) {
				newShot = candidateShots.get(random.nextInt(candidateShots.size()));
				if (!shots.contains(newShot))
					shots.add(newShot);
			}
			while (shots.size() < Game.NUMBER_SHOTS)
				shots.add(newShot);
		}

		System.out.print(Messages.get("move.burst.label") + " ");
		for (IPosition shot : shots)
			System.out.print(shot + " ");
		System.out.println();

		this.fireShots(shots);

		return ShotJsonSerializer.jsonShots(shots);
	}


	/**
	 * Reads and processes the enemy fire input from the specified scanner.
	 * The method expects input describing positions for enemy shots. It verifies
	 * the format, ensures the correct number of positions are provided, and then fires
	 * on those positions.
	 *
	 * @param in the scanner object to read the enemy fire positions from, input must
	 *           be formatted either as a single token combining the column and row
	 *           (e.g., "A3") or as separate tokens (e.g., "A" followed by "3").
	 * @throws IllegalArgumentException if the provided positions are incomplete,
	 *                                  incorrectly formatted, or do not match the
	 *                                  required number of shots (NUMBER_SHOTS).
	 */
	public String readEnemyFire(Scanner in) {

		assert in != null;

		String input = in.nextLine().trim();

		List<IPosition> shots = new ArrayList<>();

		Scanner inputScanner = new Scanner(input);
		while (shots.size() < NUMBER_SHOTS && inputScanner.hasNext()) {
			String token = inputScanner.next();

			if (token.matches("[A-Za-z]")) {
				if (inputScanner.hasNextInt()) {
					int row = inputScanner.nextInt();
					shots.add(new Position(token.toUpperCase().charAt(0), row));
				} else {
					throw new IllegalArgumentException(
							Messages.format("error.incompletePosition", token));
				}
			} else {
				Scanner singleScanner = new Scanner(token);
				shots.add(Tasks.readClassicPosition(singleScanner));
			}
		}

		if (shots.size() != NUMBER_SHOTS) {
			throw new IllegalArgumentException(
					Messages.format("error.invalidPositionCount", NUMBER_SHOTS));
		}

		this.fireShots(shots);

		return ShotJsonSerializer.jsonShots(shots);
	}

	/**
	 * Fires a set of shots during a player's move. Each shot is resolved and
	 * consolidated into a move, which is processed and added to the list of alien moves.
	 * The method ensures exactly {@code NUMBER_SHOTS} shots are fired, validates
	 * each shot's position, and increments the move counter after completing the operation.
	 *
	 * @param shots a list of positions representing the locations to fire shots at.
	 *              The positions should be unique and valid within the bounds of the game board.
	 *              The size of the list must be equal to {@code NUMBER_SHOTS}.
	 * @throws IllegalArgumentException if the list of shots is null, contains an invalid
	 *                                  number of positions, or includes duplicate positions.
	 */
	public void fireShots(List<IPosition> shots)
	{
		assert shots != null;

		List<ShotResult> shotResults = new ArrayList<ShotResult>();
		if (shots.size() != NUMBER_SHOTS) {
			throw new IllegalArgumentException(
					Messages.format("error.mustFireExactly", NUMBER_SHOTS));
		}

		List<IPosition> alreadyShot = new ArrayList<IPosition>();
		for (IPosition pos : shots) {
			shotResults.add(fireSingleShot(pos, alreadyShot.contains(pos)));
			alreadyShot.add(pos);
		}

		Move move = new Move(moveNumber, shots, shotResults);

		move.processEnemyFire(true);

		alienMoves.add(move);

		moveNumber++;
	}

	/**
	 * Fires a single shot at the specified position, handling scenarios such as invalid positions,
	 * repeated shots, hits, misses, and sinking a ship. The method updates the necessary counters
	 * for invalid shots, repeated shots, hits, and sunk ships.
	 *
	 * @param pos the position to fire the shot at; must be valid and within the game board boundaries.
	 * @param isRepeated true if the shot is marked as a repeat attempt, false otherwise.
	 * @return a ShotResult object containing the result of the shot, including whether the shot was
	 *         valid, repeated, a hit, and whether a ship was sunk.
	 */
	public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) {

		assert pos != null;

		if (!pos.isInside()) {
			countInvalidShots++;
			return new ShotResult(false, false, null, false);
		}

		if (isRepeated || repeatedShot(pos)) {
			countRepeatedShots++;
			return new ShotResult(true, true, null, false);
		}

		IShip ship = myFleet.shipAt(pos);

		if (ship == null) {
			return new ShotResult(true, false, null, false);
		}

		ship.shoot(pos);
		countHits++;

		if (!ship.stillFloating()) {
			countSinks++;
			return new ShotResult(true, false, ship, true);
		}

		SoundManager.playHit();

		return new ShotResult(true, false, ship, false);
	}

	@Override
	public int getRepeatedShots()
	{
		return this.countRepeatedShots;
	}

	@Override
	public int getInvalidShots()
	{
		return this.countInvalidShots;
	}

	@Override
	public int getHits()
	{
		return this.countHits;
	}

	@Override
	public int getSunkShips()
	{
		return this.countSinks;
	}

	@Override
	public int getRemainingShips()
	{
		List<IShip> floatingShips = myFleet.getFloatingShips();
		return floatingShips.size();
	}

	public boolean repeatedShot(IPosition pos)
	{
		assert pos != null;

		for (IMove move : alienMoves)
			if (move.getShots().contains(pos))
				return true;
		return false;
	}

	public void printMyBoard(boolean show_shots, boolean show_legend) {
		BoardPrinter.printBoard(
				this.myFleet, this.alienMoves, show_shots, show_legend);
	}

	public void printAlienBoard(boolean show_shots, boolean show_legend) {
		BoardPrinter.printBoard(
				this.alienFleet, this.myMoves, show_shots, show_legend);
	}

	public void over() {
		System.out.println();
		System.out.println(Messages.get("game.over.line1"));
		System.out.println(Messages.get("game.over.message"));
		System.out.println(Messages.get("game.over.line2"));
	}
}