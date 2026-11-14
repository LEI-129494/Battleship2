package restserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import restserver.Dtos.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtosTest {

    @Test
    @DisplayName("Deve testar os DTOs de Registo")
    void testRegistrationDtos() {
        RegistrationRequest req = new RegistrationRequest();
        req.playerName = "Alice";
        req.callbackUrl = "http://host";
        assertEquals("Alice", req.playerName);
        assertEquals("http://host", req.callbackUrl);

        RegistrationResponse res = new RegistrationResponse();
        res.gameId = "id";
        res.status = "OK";
        res.shotsPerTurn = 3;
        res.message = "Ready";
        assertEquals("id", res.gameId);
        assertEquals("OK", res.status);
        assertEquals(3, res.shotsPerTurn);
        assertEquals("Ready", res.message);
    }

    @Test
    @DisplayName("Deve testar os DTOs de Tiro (Request e Response)")
    void testShotDtos() {
        ShotPosition pos = new ShotPosition();
        pos.row = "A";
        pos.column = 1;
        assertEquals("A", pos.row);
        assertEquals(1, pos.column);

        ShotRequest req = new ShotRequest();
        req.shots = List.of(pos);
        assertNotNull(req.shots);

        ShotResult result = new ShotResult();
        result.row = "B";
        result.column = 2;
        result.outcome = "HIT";
        result.shipType = "Fragata";
        assertEquals("B", result.row);
        assertEquals(2, result.column);
        assertEquals("HIT", result.outcome);
        assertEquals("Fragata", result.shipType);

        ShotResponse res = new ShotResponse();
        res.results = List.of(result);
        res.shipsRemaining = 5;
        res.gameStatus = "ONGOING";
        res.winner = null;
        assertNotNull(res.results);
        assertEquals(5, res.shipsRemaining);
        assertEquals("ONGOING", res.gameStatus);
        assertNull(res.winner);
    }

    @Test
    @DisplayName("Deve testar o DTO de Erro")
    void testErrorResponse() {
        ErrorResponse err = new ErrorResponse("Erro grave");
        assertEquals("Erro grave", err.error);
    }

    @Test
    @DisplayName("Deve instanciar a classe externa Dtos")
    void testDtosOuterClass() {
        Dtos dtos = new Dtos();
        assertNotNull(dtos, "A classe externa Dtos deve ser instanciada para cobertura");
    }
}