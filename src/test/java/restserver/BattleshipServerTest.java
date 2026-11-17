package restserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BattleshipServerTest {

    @Test
    @DisplayName("Testar o arranque do servidor sem ligar a porta web")
    void testMain() {
        // Passamos um argumento que diz ao Spring para NÃO ligar o Tomcat (porta 8080)
        String[] args = {"--spring.main.web-application-type=none"};

        // O assertDoesNotThrow garante que a linha do main corre sem erros
        assertDoesNotThrow(() -> {
            BattleshipServer.main(args);
        });
    }
}