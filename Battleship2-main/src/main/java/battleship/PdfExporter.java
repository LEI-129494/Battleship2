package battleship;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exporta o histórico de jogadas de uma partida de Battleship para um ficheiro PDF.
 *
 * <p>Usa a biblioteca Apache PDFBox 3.x. O PDF é gravado na diretoria {@code output/}
 * com um nome do tipo {@code battleship_YYYYMMDD_HHmmss.pdf}.</p>
 *
 * <p>Conteúdo do PDF:</p>
 * <ul>
 *   <li>Cabeçalho com data/hora e resumo da partida;</li>
 *   <li>Lista das jogadas do inimigo (tiros no meu tabuleiro);</li>
 *   <li>Lista das minhas jogadas (tiros no tabuleiro inimigo).</li>
 * </ul>
 */
public class PdfExporter {

    /** Diretoria onde o PDF é gravado. */
    private static final String OUTPUT_DIR = "output";

    /** Prefixo do nome do ficheiro gerado. */
    private static final String FILE_PREFIX = "battleship_";

    /** Formato do timestamp usado no nome do ficheiro. */
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** Formato do timestamp mostrado no corpo do PDF. */
    private static final DateTimeFormatter HUMAN_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // --- Layout ---
    private static final float MARGIN = 50f;
    private static final float LINE_HEIGHT = 14f;
    private static final float FONT_SIZE_TITLE = 18f;
    private static final float FONT_SIZE_SECTION = 12f;
    private static final float FONT_SIZE_TEXT = 10f;

    /**
     * Gera o PDF com o histórico de jogadas da partida indicada.
     *
     * @param game a partida a exportar (não pode ser {@code null})
     * @return o caminho do PDF gerado
     * @throws IOException se a escrita do ficheiro falhar
     */
    public static Path export(IGame game) throws IOException {
        if (game == null) throw new IllegalArgumentException("game must not be null");

        // Garantir que a diretoria existe
        Path dir = Paths.get(OUTPUT_DIR);
        Files.createDirectories(dir);

        String filename = FILE_PREFIX + LocalDateTime.now().format(TIMESTAMP) + ".pdf";
        Path target = dir.resolve(filename);

        try (PDDocument doc = new PDDocument()) {
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = page.getMediaBox().getHeight() - MARGIN;

            // --- Cabeçalho ---
            y = writeLine(cs, bold, FONT_SIZE_TITLE, MARGIN, y,
                    "Battleship - Historico de Jogadas");
            y -= LINE_HEIGHT / 2;
            y = writeLine(cs, regular, FONT_SIZE_TEXT, MARGIN, y,
                    "Gerado em: " + LocalDateTime.now().format(HUMAN_TIMESTAMP));

            // --- Resumo da partida ---
            y -= LINE_HEIGHT;
            y = writeLine(cs, regular, FONT_SIZE_TEXT, MARGIN, y,
                    String.format(
                            "Navios a flutuar: %d | Afundados: %d | Tiros certeiros: %d | Repetidos: %d | Invalidos: %d",
                            game.getRemainingShips(), game.getSunkShips(),
                            game.getHits(), game.getRepeatedShots(), game.getInvalidShots()));

            // --- Jogadas do inimigo ---
            y -= LINE_HEIGHT * 2;
            y = writeLine(cs, bold, FONT_SIZE_SECTION, MARGIN, y,
                    "Jogadas do inimigo (tiros no meu tabuleiro):");
            y -= LINE_HEIGHT / 2;

            for (IMove move : game.getAlienMoves()) {
                if (y < MARGIN + LINE_HEIGHT) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - MARGIN;
                }
                y = writeLine(cs, regular, FONT_SIZE_TEXT, MARGIN, y, describeMove(move));
            }

            // --- Minhas jogadas ---
            y -= LINE_HEIGHT;
            if (y < MARGIN + LINE_HEIGHT) {
                cs.close();
                page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                cs = new PDPageContentStream(doc, page);
                y = page.getMediaBox().getHeight() - MARGIN;
            }
            y = writeLine(cs, bold, FONT_SIZE_SECTION, MARGIN, y,
                    "Minhas jogadas (tiros no tabuleiro inimigo):");
            y -= LINE_HEIGHT / 2;

            for (IMove move : game.getMyMoves()) {
                if (y < MARGIN + LINE_HEIGHT) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - MARGIN;
                }
                y = writeLine(cs, regular, FONT_SIZE_TEXT, MARGIN, y, describeMove(move));
            }

            cs.close();
            doc.save(target.toFile());
        }

        return target;
    }

    /**
     * Escreve uma linha de texto no PDF e devolve o {@code y} atualizado
     * (já subtraída a altura da linha).
     */
    private static float writeLine(PDPageContentStream cs, PDType1Font font, float size,
                                   float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(sanitize(text));
        cs.endText();
        return y - LINE_HEIGHT;
    }

    /**
     * Constrói a descrição textual de uma jogada, com os tiros e o resumo dos resultados.
     */
    private static String describeMove(IMove move) {
        StringBuilder sb = new StringBuilder();
        sb.append("Jogada ").append(move.getNumber()).append(": ");

        List<IPosition> shots = move.getShots();
        for (int i = 0; i < shots.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(shots.get(i).toString());
        }

        int hits = 0, misses = 0, repeated = 0;
        for (IGame.ShotResult r : move.getShotResults()) {
            if (!r.valid()) continue;
            if (r.repeated()) repeated++;
            else if (r.ship() != null) hits++;
            else misses++;
        }

        sb.append("  ->  acertos=").append(hits)
          .append(", agua=").append(misses)
          .append(", repetidos=").append(repeated);

        return sb.toString();
    }

    /**
     * As fontes standard-14 do PDFBox apenas suportam WinAnsi.
     * Remove caracteres de controlo e substitui tabs por espaço.
     */
    private static String sanitize(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\t') sb.append(' ');
            else if (c < 32) continue;
            else sb.append(c);
        }
        return sb.toString();
    }
}
