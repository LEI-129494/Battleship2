package battleship;

import java.util.Scanner;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * The type Tasks.
 */
public class Tasks {

	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	// ---------------------------------------------------------------
	// Identificadores canonicos (internos, independentes do idioma).
	// As strings que o utilizador escreve sao lidas do bundle de
	// mensagens (chaves cmd.*) e podem diferir entre idiomas,
	// ex.: "ajuda" (pt) vs "help" (en).
	// ---------------------------------------------------------------
	private static final String CMD_HELP      = "HELP";
	private static final String CMD_NEWFLEET  = "NEWFLEET";
	private static final String CMD_LOADFLEET = "LOADFLEET";
	private static final String CMD_STATUS    = "STATUS";
	private static final String CMD_MAP       = "MAP";
	private static final String CMD_SHOT      = "SHOT";
	private static final String CMD_SHOTS     = "SHOTS";
	private static final String CMD_SIMULATE  = "SIMULATE";
	private static final String CMD_EXPORTPDF = "EXPORTPDF";
	private static final String CMD_QUIT      = "QUIT";
	private static final String CMD_UNKNOWN   = "";


	/**
	 * Apresenta um menu bilingue de escolha de idioma no arranque do jogo e
	 * reinicializa o bundle de mensagens com o locale escolhido.
	 *
	 * <p>Este menu é intencionalmente bilingue (nao traduzido) porque, por
	 * definicao, o idioma ainda nao foi escolhido. Depois desta chamada, todas
	 * as mensagens subsequentes sao obtidas no idioma selecionado.</p>
	 *
	 * <p>Aceita "1" ou "pt" para Portugues e "2" ou "en" para Ingles. Qualquer
	 * outra entrada e tratada como Portugues (idioma por omissao).</p>
	 */
	public static void chooseLanguage() {
		System.out.println("==========================================");
		System.out.println(" Escolha o idioma / Choose language:");
		System.out.println("   1 - Portugues");
		System.out.println("   2 - English");
		System.out.println("==========================================");
		System.out.print("> ");

		Scanner in = new Scanner(System.in);
		String opt = in.next().trim().toLowerCase();

		if ("2".equals(opt) || "en".equals(opt)) {
			Messages.init(Locale.ENGLISH);
		} else {
			Messages.init(Locale.of("pt"));
		}
		System.out.println();
	}

	/**
	 * This task also tests the fighting element of a round of three shots
	 */
	public static void menu() {

		IFleet myFleet = null;
		IGame game = null;
		menuHelp();

		System.out.print("> ");
		Scanner in = new Scanner(System.in);
		String command = in.next();
		while (!CMD_QUIT.equals(canonical(command))) {

			switch (canonical(command)) {
				case CMD_NEWFLEET:
					myFleet = Fleet.createRandom();
					game = new Game(myFleet);
					game.printMyBoard(false, true);
					break;
				case CMD_LOADFLEET:
					myFleet = buildFleet(in);
					game = new Game(myFleet);
					game.printMyBoard(false, true);
					break;
				case CMD_STATUS:
					if (myFleet != null)
						myFleet.printStatus();
					break;
				case CMD_MAP:
					if (myFleet != null)
						game.printMyBoard(false, true);
					break;
				case CMD_SHOT:
					if (game != null) {
						game.readEnemyFire(in);
						myFleet.printStatus();
						game.printMyBoard(true, false);

						if (game.getRemainingShips() == 0) {
							game.over();
							System.exit(0);
						}
					}
					break;
				case CMD_SIMULATE:
					if (game != null) {
						while (game.getRemainingShips() > 0){
							game.randomEnemyFire();
							myFleet.printStatus();
							game.printMyBoard(true, false);
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
						}

						if (game.getRemainingShips() == 0) {
							game.over();
							System.exit(0);
						}
					}
					break;
				case CMD_SHOTS:
					if (game != null)
						game.printMyBoard(true, true);
					break;
				case CMD_HELP:
					menuHelp();
					break;
				default:
					System.out.println(Messages.get("msg.unknownCommand"));
			}
			System.out.print("> ");
			command = in.next();
		}
		System.out.println(Messages.get("msg.goodbye"));
	}

	/**
	 * Maps a user-typed token (in the currently active language) to the
	 * canonical, language-independent command identifier used by the
	 * {@code switch} in {@link #menu()}.
	 *
	 * @param input raw token typed by the user (case-insensitive)
	 * @return one of the {@code CMD_*} constants, or {@link #CMD_UNKNOWN} if the
	 *         token does not match any known command in the active language
	 */
	private static String canonical(String input) {
		if (input == null) return CMD_UNKNOWN;
		String t = input.toLowerCase();
		if (t.equals(Messages.get("cmd.help").toLowerCase()))      return CMD_HELP;
		if (t.equals(Messages.get("cmd.newfleet").toLowerCase()))  return CMD_NEWFLEET;
		if (t.equals(Messages.get("cmd.loadfleet").toLowerCase())) return CMD_LOADFLEET;
		if (t.equals(Messages.get("cmd.status").toLowerCase()))    return CMD_STATUS;
		if (t.equals(Messages.get("cmd.map").toLowerCase()))       return CMD_MAP;
		if (t.equals(Messages.get("cmd.shot").toLowerCase()))      return CMD_SHOT;
		if (t.equals(Messages.get("cmd.shots").toLowerCase()))     return CMD_SHOTS;
		if (t.equals(Messages.get("cmd.simulate").toLowerCase()))  return CMD_SIMULATE;
		if (t.equals(Messages.get("cmd.quit").toLowerCase()))      return CMD_QUIT;
		return CMD_UNKNOWN;
	}

	/**
	 * This function provides help information about the menu commands.
	 * The command names shown are those of the active language.
	 */
	public static void menuHelp() {
		System.out.println(Messages.get("menu.help.title"));
		System.out.println(Messages.get("menu.help.intro"));
		System.out.println(Messages.format("menu.help.help",      Messages.get("cmd.help")));
		System.out.println(Messages.format("menu.help.newfleet",  Messages.get("cmd.newfleet")));
		System.out.println(Messages.format("menu.help.loadfleet", Messages.get("cmd.loadfleet")));
		System.out.println(Messages.format("menu.help.status",    Messages.get("cmd.status")));
		System.out.println(Messages.format("menu.help.map",       Messages.get("cmd.map")));
		System.out.println(Messages.format("menu.help.shot",      Messages.get("cmd.shot")));
		System.out.println(Messages.format("menu.help.simulate",  Messages.get("cmd.simulate")));
		System.out.println(Messages.format("menu.help.shots",     Messages.get("cmd.shots")));
		System.out.println(Messages.format("menu.help.quit",      Messages.get("cmd.quit")));
		System.out.println(Messages.get("menu.help.footer"));
	}

	/**
	 * This operation allows the build up of a fleet, given user data
	 *
	 * @param in The scanner to read from
	 * @return The fleet that has been built
	 */
	public static Fleet buildFleet(Scanner in) {

		assert in != null;

		Fleet fleet = new Fleet();
		int i = 0; // i represents the total of successfully created ships
		while (i < Fleet.FLEET_SIZE) {
			IShip s = readShip(in);
			if (s != null) {
				boolean success = fleet.addShip(s);
				if (success)
					i++;
				else
					LOGGER.info(Messages.format("msg.shipAddFailure",
							s.getCategory(), s.getBearing(), s.getPosition()));
			} else {
				LOGGER.info(Messages.get("msg.unknownShip"));
			}
		}
		LOGGER.info(Messages.format("msg.shipsAdded", i));
		return fleet;
	}

	/**
	 * This operation reads data about a ship, build it and returns it
	 *
	 * @param in The scanner to read from
	 * @return The created ship based on the data that has been read
	 */
	public static Ship readShip(Scanner in) {

		assert in != null;

		String shipKind = in.next();
		Position pos = readPosition(in);
		char c = in.next().charAt(0);
		Compass bearing = Compass.charToCompass(c);
		return Ship.buildShip(shipKind, bearing, pos);
	}

	/**
	 * This operation allows reading a position in the map
	 *
	 * @param in The scanner to read from
	 * @return The position that has been read
	 */
	public static Position readPosition(Scanner in) {

		assert in != null;

		int row = in.nextInt();
		int column = in.nextInt();
		return new Position(row, column);
	}

	/**
	 * This operation allows reading a position in the map
	 *
	 * @param in The scanner to read from
	 * @return The classic position that has been read
	 */
	public static IPosition readClassicPosition(@NotNull Scanner in) {
		// Verifica se ainda ha tokens disponiveis
		if (!in.hasNext()) {
			throw new IllegalArgumentException(Messages.get("error.noValidPosition"));
		}

		String part1 = in.next(); // Primeiro token
		String part2 = null;

		if (in.hasNextInt()) {
			part2 = in.next(); // Segundo token, se disponivel
		}

		String input = (part2 != null) ? part1 + part2 : part1;

		// Normalizar o input para tratar letras maiusculas e minusculas
		input = input.toUpperCase();

		// Verificar os dois formatos possiveis: compactos e com espaco
		if (input.matches("[A-Z]\\d+")) {
			char column = input.charAt(0); // Extrair a coluna
			int row = Integer.parseInt(input.substring(1)); // Extrair a linha
			return new Position(column, row);
		} else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
			char column = part1.charAt(0); // Extrair a coluna
			int row = Integer.parseInt(part2); // Extrair a linha
			return new Position(column, row);
		} else {
			throw new IllegalArgumentException(Messages.get("error.invalidPositionFormat"));
		}
	}

}
