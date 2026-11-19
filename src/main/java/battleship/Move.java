
package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

public class Move implements IMove {

	private final int number;
	private final List<IPosition> shots;
	private final List<IGame.ShotResult> shotResults;

	public Move(int moveNumber, List<IPosition> moveShots,
				List<IGame.ShotResult> moveResults) {
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

	@Override
	public String processEnemyFire(boolean verbose) {

		int validShots = 0;
		int repeatedShots = 0;
		int missedShots = 0;

		Map<String, Integer> sunkBoatsCount = new HashMap<>();
		Map<String, Integer> hitsPerBoat = new HashMap<>();

		// Processar cada resultado de tiro
		for (int i = 0; i < this.shotResults.size(); i++) {
			IGame.ShotResult result = this.shotResults.get(i);
			IPosition position = this.shots.get(i);
			String databaseResult;

			if (!result.valid()) {
				databaseResult = "OUTSIDE";
			} else if (result.repeated()) {
				repeatedShots++;
				databaseResult = "REPEATED";
			} else {
				validShots++;

				if (result.ship() == null) {
					missedShots++;
					databaseResult = "MISS";
				} else {
					String boatName = result.ship().getCategory();

					hitsPerBoat.put(
							boatName,
							hitsPerBoat.getOrDefault(boatName, 0) + 1
					);

					if (result.sunk()) {
						sunkBoatsCount.put(
								boatName,
								sunkBoatsCount.getOrDefault(boatName, 0) + 1
						);
						databaseResult = "SUNK";
					} else {
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

		// Determinar número de tiros fora do tabuleiro
		int outsideShots = Game.NUMBER_SHOTS - validShots - repeatedShots;

		MoveStatistics statistics = new MoveStatistics(
				validShots,
				repeatedShots,
				missedShots,
				outsideShots,
				sunkBoatsCount,
				hitsPerBoat
		);

		if (verbose) {
			String summary = buildSummary(statistics);

			System.out.println(
					Messages.format("move.line", this.number, summary)
			);
		}

		// Construção da resposta JSON
		Map<String, Object> response = buildResponse(statistics);

		String jsonString = serializeResponse(response);

		System.out.println(jsonString);
		System.out.println();

		return jsonString;
	}

	/**
	 * Constrói o mapa com os resultados dos disparos.
	 */
	private Map<String, Object> buildResponse(MoveStatistics statistics) {

		Map<String, Object> response = new HashMap<>();
		response.put("validShots", statistics.validShots());
		response.put("outsideShots", statistics.outsideShots());
		response.put("repeatedShots", statistics.repeatedShots());
		response.put("missedShots", statistics.missedShots());

		List<Map<String, Object>> sunkBoats = new ArrayList<>();

		for (Map.Entry<String, Integer> entry :
				statistics.sunkBoatsCount().entrySet()) {

			Map<String, Object> boat = new HashMap<>();
			boat.put("type", entry.getKey());
			boat.put("count", entry.getValue());
			sunkBoats.add(boat);
		}

		response.put("sunkBoats", sunkBoats);

		List<Map<String, Object>> boatHits = new ArrayList<>();

		for (Map.Entry<String, Integer> entry :
				statistics.hitsPerBoat().entrySet()) {

			if (!statistics.sunkBoatsCount().containsKey(entry.getKey())) {
				Map<String, Object> boat = new HashMap<>();
				boat.put("type", entry.getKey());
				boat.put("hits", entry.getValue());
				boatHits.add(boat);
			}
		}

		response.put("hitsOnBoats", boatHits);

		return response;
	}

	/**
	 * Serializa a resposta para JSON.
	 *
	 * @param response mapa com os resultados dos disparos
	 * @return resposta em formato JSON
	 */
    String serializeResponse(Map<String, Object> response) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		try {
			return objectMapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(
					Messages.get("error.jsonMoveSerialization"), e
			);
		}
	}

	/**
	 * Constrói a mensagem legível para o jogador.
	 */
	private String buildSummary(MoveStatistics statistics) {

		StringBuilder sb = new StringBuilder();

		// Caso especial: só há tiros repetidos
		if (statistics.validShots() == 0 &&
				statistics.repeatedShots() > 0) {

			sb.append(Messages.format(
					"move.repeated",
					countArgs(statistics.repeatedShots())
			));

		} else {
			List<String> parts = new ArrayList<>();

			// Navios afundados
			for (Map.Entry<String, Integer> e :
					statistics.sunkBoatsCount().entrySet()) {

				Map<String, Object> args = new HashMap<>();
				args.put("count", e.getValue());
				args.put("type", shipName(e.getKey(), false));
				args.put("typePlural", shipName(e.getKey(), true));

				parts.add(Messages.format("move.sunk", args));
			}

			// Navios atingidos mas ainda a flutuar
			for (Map.Entry<String, Integer> e :
					statistics.hitsPerBoat().entrySet()) {

				if (statistics.sunkBoatsCount().containsKey(e.getKey())) {
					continue;
				}

				Map<String, Object> args = new HashMap<>();
				args.put("count", e.getValue());
				args.put("type", shipName(e.getKey(), false));

				parts.add(Messages.format("move.hits", args));
			}

			// Tiros na água
			if (statistics.missedShots() > 0) {
				parts.add(Messages.format(
						"move.missed",
						countArgs(statistics.missedShots())
				));
			}

			// Prefixo dos tiros válidos
			if (statistics.validShots() > 0) {
				sb.append(Messages.format(
						"move.valid",
						countArgs(statistics.validShots())
				));
				sb.append(": ");
			}

			sb.append(String.join(
					" " + Messages.get("move.joiner") + " ",
					parts
			));

			// Tiros repetidos
			if (statistics.repeatedShots() > 0) {
				sb.append(", ");
				sb.append(Messages.format(
						"move.repeated",
						countArgs(statistics.repeatedShots())
				));
			}
		}

		// Tiros exteriores
		if (statistics.outsideShots() > 0) {
			if (sb.length() > 0) {
				sb.append(", ");
			}

			sb.append(Messages.format(
					"move.outside",
					countArgs(statistics.outsideShots())
			));
		}

		return sb.toString();
	}

	/**
	 * Devolve o nome traduzido de um tipo de navio.
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
	 * Cria um mapa de argumentos ICU com a chave count.
	 */
	private static Map<String, Object> countArgs(int count) {
		Map<String, Object> args = new HashMap<>();
		args.put("count", count);
		return args;
	}
}
