package battleship;

import com.ibm.icu.text.MessageFormat;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Ponto único de acesso às mensagens internacionalizadas do jogo.
 *
 * <p>Esta classe encapsula o carregamento do {@link ResourceBundle} e a formatação
 * das mensagens com o {@link MessageFormat} do ICU4J, que suporta regras de plural
 * dependentes do idioma (ex.: "1 tiro" vs "3 tiros").</p>
 *
 * <p>Idiomas suportados (ver ficheiros em {@code src/main/resources}):</p>
 * <ul>
 *   <li>{@code pt} — Português (idioma por omissão)</li>
 *   <li>{@code en} — Inglês</li>
 * </ul>
 *
 * <p>Para forçar um idioma sem alterar o código, o utilizador pode correr o JAR com
 * a propriedade de sistema {@code -Duser.language=en} (ou {@code pt}).</p>
 *
 * @author LEI-124579
 */
public final class Messages {

    /**
     * Nome base dos ficheiros de mensagens. O Maven coloca-os em
     * {@code target/classes/messages_<lang>.properties} a partir de
     * {@code src/main/resources/messages_<lang>.properties}.
     */
    private static final String BUNDLE_NAME = "messages";

    /**
     * Locale usado como último recurso caso o pedido não tenha bundle associado.
     */
    private static final Locale FALLBACK_LOCALE = Locale.of("pt");

    private static ResourceBundle bundle;
    private static Locale currentLocale;

    static {
        // Por omissão, arranca com o locale da JVM. Isto permite que o utilizador
        // escolha o idioma via "-Duser.language=en" sem tocar no código.
        init(Locale.getDefault());
    }

    private Messages() {
        // Classe utilitária — não deve ser instanciada.
    }

    /**
     * (Re)inicializa o idioma ativo. Deve ser chamada no arranque ou sempre que o
     * utilizador troca de idioma em tempo de execução.
     *
     * <p>Se o locale pedido não tiver ficheiro {@code .properties} correspondente,
     * é aplicado o fallback para Português. Se, por algum motivo, também não
     * existir bundle em Português, é lançada uma {@link MissingResourceException}
     * — o que indicaria um problema de empacotamento, não de utilizador.</p>
     *
     * @param locale locale pretendido; se {@code null}, usa o da JVM
     */
    public static void init(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            currentLocale = locale;
        } catch (MissingResourceException e) {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, FALLBACK_LOCALE);
            currentLocale = FALLBACK_LOCALE;
        }
    }

    /**
     * Devolve o locale atualmente ativo. Útil para lógica condicional (ex.: escolher
     * o idioma dos comandos do menu de consola).
     *
     * @return o {@link Locale} em uso
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Devolve a mensagem associada à chave, sem formatação.
     *
     * @param key chave definida nos ficheiros {@code messages_*.properties}
     * @return a mensagem traduzida
     * @throws MissingResourceException se a chave não existir no bundle ativo
     */
    public static String get(String key) {
        return bundle.getString(key);
    }

    /**
     * Devolve a mensagem associada à chave, aplicando formatação ICU
     * ({@link MessageFormat}). Suporta plurais do tipo:
     * <pre>{@code
     *   msg.shots={count, plural, =0 {sem tiros} one {# tiro} other {# tiros}}
     * }</pre>
     * e placeholders posicionais do tipo {@code {0}}, {@code {1}}.
     *
     * @param key  chave definida nos ficheiros {@code messages_*.properties}
     * @param args argumentos a substituir no padrão
     * @return a mensagem traduzida e formatada
     */
    public static String format(String key, Object... args) {
        String pattern = bundle.getString(key);
        MessageFormat mf = new MessageFormat(pattern, currentLocale);
        return mf.format(args);
    }

    /**
     * Variante de {@link #format(String, Object...)} que aceita argumentos nomeados.
     * Permite usar templates ICU com nomes em vez de posições, por exemplo:
     * <pre>{@code
     *   move.sunk={count, plural, one {# {type} ao fundo} other {# {typePlural} ao fundo}}
     * }</pre>
     *
     * @param key  chave definida nos ficheiros {@code messages_*.properties}
     * @param args mapa de nome -> valor a substituir no padrao
     * @return a mensagem traduzida e formatada
     */
    public static String format(String key, java.util.Map<String, ?> args) {
        String pattern = bundle.getString(key);
        MessageFormat mf = new MessageFormat(pattern, currentLocale);
        StringBuffer sb = new StringBuffer();
        mf.format(args, sb, null);
        return sb.toString();
    }
}