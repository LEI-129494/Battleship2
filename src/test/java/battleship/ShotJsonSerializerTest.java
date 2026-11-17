package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ShotJsonSerializerTest {

    @Test
    @DisplayName("Testar conversão de lista de posições para JSON")
    void testJsonShots() {
        // Inicializar as mensagens (necessário para o sistema)
        Messages.init(java.util.Locale.ENGLISH);

        // Criar uma lista de tiros (posições)
        List<IPosition> tiros = new ArrayList<>();
        tiros.add(new Position(1, 1));
        tiros.add(new Position('B', 2));

        // Chamar o método
        String json = ShotJsonSerializer.jsonShots(tiros);

        // Verificar se o JSON foi gerado corretamente
        assertNotNull(json);
        assertTrue(json.contains("\"row\""));
        assertTrue(json.contains("\"column\""));
    }
}