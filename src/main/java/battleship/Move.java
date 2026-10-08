package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

/**
 * Shot
 *
 * @author Your Name
 * Date: 20/02/2026
 * Time: 19:39
 */
public class Move implements IMove {

	//-------------------------------------------------------------------
	private final int number;
	private final List<IPosition> shots;
	private final List<IGame.ShotResult> shotResults;

	//-------------------------------------------------------------------
	public Move(int moveNumber, List<IPosition> moveShots, List<IGame.ShotResult> moveResults) {
		this.number = moveNumber;
		this.shots = moveShots;
		this.shotResults = moveResults;
	}

	@Override
	public String toString() {
		return "Move{" +
				"number=" + number +
				", shots=" + shots.size() +
				", results=" + shotResults.size() +
				'}';
	}

	@Override
	public int getNumber() {
		return this.number;
	}

	@Override
	public List<IPosition> getShots() {
		return this.shots;
	}

	@Override
	public List<IGame.ShotResult> getShotResults() {
		return this.shotResults;
	}

	/**
	 * Processes the results of enemy fire on the game board, analyzing the outcomes of shots,
	 * such as valid shots, repeated shots, missed shots, hits on ships, and sunk ships. It can
	 * also display a detailed summary of the shot results if verbose mode is activated.
	 *
	 * <p>The human-readable summary is built with the ICU4J {@code MessageFormat}, using
	 * {@code plural} rules to correctly handle singular/plural agreement in any language.
	 * The active language is determined by {@link Messages#getCurrentLocale()}.</p>
	 *
	 * @param verbose a boolean indicating whether a detailed summary should be printed to the console
	 *                for the processed enemy fire data.
	 * @return a JSON-formatted string that encapsulates the results, including counts of valid shots,
	 *         repeated shots, missed shots, shots outside the game board, and details of hits and
	 *         sunk ships.
	 */
	@Override
	public String processEnemyFire(boolean verbose) {

		int validShots = 0;
		int repeatedShots = 0;
		int missedShots = 0;

		Map<String, Integer> sunkBoatsCount = new HashMap<>(); // Quantos navios de cada tipo afundaram
		Map<String, Integer> hitsPerBoat = new HashMap<>();

		// Processar cada resultado de tiro
		for (int i = 0; i < this.shotResults.size(); i++) {
			IGame.ShotResult result = this.shotResults.get(i);
			IPosition position = this.shots.get(i);
			String databaseResult;
			if (!result.valid()) {
				// Tiro invalido - apenas ignorar
				databaseResult = "OUTSIDE";
			}

			else if (result.repeated()) {
				repeatedShots++; // tiro repetido
				databaseResult = "REPEATED";
			}
			else {
				// Tiro valido
				validShots++;
				if (result.ship() == null) {
					missedShots++; // Tiro na agua
					databaseResult = "MISS";
				}
				else {
					String boatName = result.ship().getCategory();
					hitsPerBoat.put(boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1);
					if (result.sunk()) {
						sunkBoatsCount.put(boatName, sunkBoatsCount.getOrDefault(boatName, 0) + 1);
						databaseResult = "SUNK";
					}else {
						databaseResult = "HIT";
					}
				}
			}
			DatabaseManager.saveShot(
					this.number,
					String.valueOf(position.getClassicRow()),
					position.getClassicColumn(),
					databaseResult
			);
		}

		// Determinar numero de tiros fora do tabuleiro
		int outsideShots = Game.NUMBER_SHOTS - validShots - repeatedShots;

		if (verbose) {
			String summary = buildSummary(validShots, repeatedShots, missedShots,
					outsideShots, sunkBoatsCount, hitsPerBoat);
			System.out.println(Messages.format("move.line", this.number, summary));
		}

		// ----------------------------------------------------------------
		// Construcao do JSON de resposta (inalterado)
		// ----------------------------------------------------------------
		Map<String, Object> response = new HashMap<>();
		response.put("validShots", validShots);
		response.put("outsideShots", outsideShots);
		response.put("repeatedShots", repeatedShots);
		response.put("missedShots", missedShots);

		List<Map<String, Object>> sunkBoats = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
			Map<String, Object> boat = new HashMap<>();
			boat.put("type", entry.getKey());
			boat.put("count", entry.getValue());
			sunkBoats.add(boat);
		}
		response.put("sunkBoats", sunkBoats);

		List<Map<String, Object>> boatHits = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
			if (!sunkBoatsCount.containsKey(entry.getKey())) {
				Map<String, Object> boat = new HashMap<>();
				boat.put("type", entry.getKey());
				boat.put("hits", entry.getValue());
				boatHits.add(boat);
			}
		}
		response.put("hitsOnBoats", boatHits);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		String jsonString;
		try {
			jsonString = objectMapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(Messages.get("error.jsonMoveSerialization"), e);
		}

		System.out.println(jsonString);
		System.out.println();

		return jsonString;
	}

	/**
	 * Constroi a mensagem legivel para o jogador, usando ICU4J {@code MessageFormat}
	 * para as regras de plural. As pecas sao montadas numa lista e juntas com
	 * {@code " + "}, tal como no formato original.
	 *
	 * <p>Exemplos de saida (em Portugues):</p>
	 * <ul>
	 *   <li>{@code 3 tiros validos: 1 Nau ao fundo + 2 tiros num(a) Caravela + 1 tiro na agua}</li>
	 *   <li>{@code 1 tiro repetido}</li>
	 *   <li>{@code 3 tiros exteriores}</li>
	 * </ul>
	 */
	private String buildSummary(int validShots, int repeatedShots, int missedShots, int outsideShots,
								Map<String, Integer> sunkBoatsCount, Map<String, Integer> hitsPerBoat) {

		StringBuilder sb = new StringBuilder();

		// Caso especial: so ha tiros repetidos (nenhum tiro valido)
		if (validShots == 0 && repeatedShots > 0) {
			sb.append(Messages.format("move.repeated", countArgs(repeatedShots)));
		} else {
			List<String> parts = new ArrayList<>();

			// Navios afundados: "N {type} ao fundo" / "N {typePlural} ao fundo"
			for (Map.Entry<String, Integer> e : sunkBoatsCount.entrySet()) {
				Map<String, Object> args = new HashMap<>();
				args.put("count", e.getValue());
				args.put("type", shipName(e.getKey(), false));
				args.put("typePlural", shipName(e.getKey(), true));
				parts.add(Messages.format("move.sunk", args));
			}

			// Navios atingidos mas ainda a flutuar (nao contam tambem como afundados)
			for (Map.Entry<String, Integer> e : hitsPerBoat.entrySet()) {
				if (sunkBoatsCount.containsKey(e.getKey())) continue;
				Map<String, Object> args = new HashMap<>();
				args.put("count", e.getValue());
				args.put("type", shipName(e.getKey(), false));
				parts.add(Messages.format("move.hits", args));
			}

			// Tiros na agua
			if (missedShots > 0) {
				parts.add(Messages.format("move.missed", countArgs(missedShots)));
			}

			// Prefixo "N tiros validos: " (so quando ha tiros validos)
			if (validShots > 0) {
				sb.append(Messages.format("move.valid", countArgs(validShots)));
				sb.append(": ");
			}

			sb.append(String.join(" " + Messages.get("move.joiner") + " ", parts));

			// Tiros repetidos a seguir, com virgula
			if (repeatedShots > 0) {
				if (sb.length() > 0) sb.append(", ");
				sb.append(Messages.format("move.repeated", countArgs(repeatedShots)));
			}
		}

		// Tiros exteriores sempre no fim
		if (outsideShots > 0) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(Messages.format("move.outside", countArgs(outsideShots)));
		}

		return sb.toString();
	}

	/**
	 * Devolve o nome traduzido de um tipo de navio, dado o seu identificador interno
	 * (o valor retornado por {@code IShip.getCategory()}, e.g. {@code "Nau"}).
	 * Faz fallback para o proprio identificador se a chave nao existir no bundle.
	 *
	 * @param category identificador interno do tipo de navio
	 * @param plural   {@code true} para devolver a forma plural
	 */
	private String shipName(String category, boolean plural) {
		String key = "ship." + category + (plural ? ".plural" : "");
		try {
			return Messages.get(key);
		} catch (MissingResourceException e) {
			return category;
		}
	}

	/**
	 * Atalho para criar um mapa de argumentos ICU com a chave {@code count}.
	 */
	private static Map<String, Object> countArgs(int count) {
		Map<String, Object> args = new HashMap<>();
		args.put("count", count);
		return args;
	}
}