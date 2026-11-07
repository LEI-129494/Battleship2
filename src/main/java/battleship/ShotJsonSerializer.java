package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShotJsonSerializer {

    public static String jsonShots(List<IPosition> shots) {
        assert shots != null;

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        List<Map<String, Object>> simplifiedShots = new ArrayList<>();

        for (IPosition shot : shots) {
            Map<String, Object> simplePos = new LinkedHashMap<>();
            simplePos.put("row", String.valueOf(shot.getClassicRow()));
            simplePos.put("column", shot.getClassicColumn());
            simplifiedShots.add(simplePos);
        }

        String jsonString;

        try {
            jsonString = objectMapper.writeValueAsString(simplifiedShots);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    Messages.get("error.jsonSerialization"), e);
        }

        System.out.println(jsonString);
        System.out.println();

        return jsonString;
    }
}