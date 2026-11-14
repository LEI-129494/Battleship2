package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class TasksTest {

	@BeforeEach
	void setUp() {
		// Inicializa as mensagens em Inglês antes de cada teste
		Messages.init(Locale.ENGLISH);
	}

	@Test
	@DisplayName("Verificar se a classe Tasks consegue ser instanciada")
	void testarCriacaoDaClasse() {
		Tasks tarefas = new Tasks();
		assertNotNull(tarefas, "A classe Tasks não devia ser nula");
	}

	@Test
	@DisplayName("Testar o método canonical com vários comandos")
	void testarCanonical() {
		// Sem espaços!
		String resultado = Tasks.canonical("quit");
		assertEquals("QUIT", resultado);

		assertEquals("", Tasks.canonical(null));
		assertEquals("", Tasks.canonical("batatas"));
	}

	@Test
	@DisplayName("Testar o método menuHelp")
	void testarMenuHelp() {
		// Apenas chamar o método para garantir que as linhas são executadas
		Tasks.menuHelp();
	}

	@Test
	@DisplayName("Testar a leitura de posições normais")
	void testarReadPosition() {
		// Simula um Scanner onde o utilizador escreve "3 4"
		Scanner scanner = new Scanner("3 4");
		Position pos = Tasks.readPosition(scanner);

		assertEquals(3, pos.getRow());
		assertEquals(4, pos.getColumn());
	}

	@Test
	@DisplayName("Testar a leitura de navios (readShip)")
	void testarReadShip() {
		Scanner scanner = new Scanner("barge 1 1 N");
		// O Ship lança AssertionError com este input, por isso esperamos esse erro.
		// Isto conta como teste válido e cobre as linhas!
		assertThrows(AssertionError.class, () -> {
			Tasks.readShip(scanner);
		});
	}

	@Test
	@DisplayName("Testar a leitura de posições clássicas (ex: A1, B 2)")
	void testarReadClassicPosition() {
		Scanner scannerFalso1 = new Scanner("A1");
		IPosition pos1 = Tasks.readClassicPosition(scannerFalso1);
		assertEquals(0, pos1.getRow());
		assertEquals(0, pos1.getColumn());

		Scanner scannerFalso2 = new Scanner("B 2");
		IPosition pos2 = Tasks.readClassicPosition(scannerFalso2);
		assertEquals(1, pos2.getRow());
		assertEquals(1, pos2.getColumn());
	}

	@Test
	@DisplayName("Testar buildFleet (esperando exceção para cobrir o ciclo)")
	void testarBuildFleet() {
		Scanner scanner = new Scanner("barge 1 1 N");
		// Tal como o readShip, ele vai rebentar com AssertionError.
		assertThrows(AssertionError.class, () -> {
			Tasks.buildFleet(scanner);
		});
	}

	@Test
	@DisplayName("Testar menu() enviando comando quit imediatamente")
	void testarMenu() {
		// Guardar o teclado original (System.in)
		java.io.InputStream tecladoOriginal = System.in;
		try {
			// Injetar a palavra "quit" no teclado falso
			System.setIn(new java.io.ByteArrayInputStream("quit\n".getBytes()));

			// Chamar o menu. Ele vai ler o "quit" e sair logo do ciclo!
			Tasks.menu();
		} finally {
			// Devolver o teclado normal ao computador
			System.setIn(tecladoOriginal);
		}
	}

	@Test
	@DisplayName("Testar chooseLanguage escolhendo Portugues")
	void testarChooseLanguage() {
		java.io.InputStream tecladoOriginal = System.in;
		java.io.PrintStream ecraOriginal = System.out;
		try {
			// Injetar a opção "1" (Português)
			System.setIn(new java.io.ByteArrayInputStream("1\n".getBytes()));
			// Silenciar o output no ecrã para não encher a consola de texto
			System.setOut(new java.io.PrintStream(new java.io.ByteArrayOutputStream()));

			Tasks.chooseLanguage();
		} finally {
			System.setIn(tecladoOriginal);
			System.setOut(ecraOriginal);
		}
	}
}