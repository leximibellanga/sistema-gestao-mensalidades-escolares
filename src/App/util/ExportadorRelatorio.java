package App.util;

import App.model.Relatorio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Exporta um Relatorio para Excel (.xlsx) e PDF sem bibliotecas externas.
 * O .xlsx é um ZIP com XML (java.util.zip) e o PDF é escrito directamente.
 */
public final class ExportadorRelatorio {

    private ExportadorRelatorio() {
    }

    public static void paraXlsx(Relatorio relatorio, File destino) throws IOException {
        Files.write(destino.toPath(), gerarXlsx(relatorio));
    }

    public static void paraPdf(Relatorio relatorio, File destino) throws IOException {
        Files.write(destino.toPath(), gerarPdf(relatorio));
    }

    // =====================================================================
    //  EXCEL (.xlsx)
    // =====================================================================

    // Estilos (índices em cellXfs)
    private static final int E_TITULO = 1;
    private static final int E_CABECALHO = 2;
    private static final int E_TEXTO = 3;
    private static final int E_INTEIRO = 4;
    private static final int E_MOEDA = 5;
    private static final int E_TOTAL_TEXTO = 6;
    private static final int E_TOTAL_INTEIRO = 7;
    private static final int E_TOTAL_MOEDA = 8;

    private static final String XML_DECL = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\n";
    private static final String NS_REL = "http://schemas.openxmlformats.org/package/2006/relationships";
    private static final String TIPO_REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/";

    private static final String CONTENT_TYPES = XML_DECL
            + "<Types xmlns='http://schemas.openxmlformats.org/package/2006/content-types'>"
            + "<Default Extension='rels' ContentType='application/vnd.openxmlformats-package.relationships+xml'/>"
            + "<Default Extension='xml' ContentType='application/xml'/>"
            + "<Override PartName='/xl/workbook.xml' ContentType='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml'/>"
            + "<Override PartName='/xl/worksheets/sheet1.xml' ContentType='application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml'/>"
            + "<Override PartName='/xl/styles.xml' ContentType='application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml'/>"
            + "</Types>";

    private static final String RELS = XML_DECL
            + "<Relationships xmlns='" + NS_REL + "'>"
            + "<Relationship Id='rId1' Type='" + TIPO_REL + "officeDocument' Target='xl/workbook.xml'/>"
            + "</Relationships>";

    private static final String WORKBOOK = XML_DECL
            + "<workbook xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main' "
            + "xmlns:r='" + TIPO_REL.substring(0, TIPO_REL.length() - 1) + "'>"
            + "<sheets><sheet name='Relatório' sheetId='1' r:id='rId1'/></sheets>"
            + "</workbook>";

    private static final String WORKBOOK_RELS = XML_DECL
            + "<Relationships xmlns='" + NS_REL + "'>"
            + "<Relationship Id='rId1' Type='" + TIPO_REL + "worksheet' Target='worksheets/sheet1.xml'/>"
            + "<Relationship Id='rId2' Type='" + TIPO_REL + "styles' Target='styles.xml'/>"
            + "</Relationships>";

    private static final String BORDA = "<border><left style='thin'><color auto='1'/></left>"
            + "<right style='thin'><color auto='1'/></right><top style='thin'><color auto='1'/></top>"
            + "<bottom style='thin'><color auto='1'/></bottom><diagonal/></border>";

    private static final String STYLES = XML_DECL
            + "<styleSheet xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main'>"
            + "<numFmts count='1'><numFmt numFmtId='164' formatCode='#,##0.00 \"MT\"'/></numFmts>"
            + "<fonts count='3'>"
            + "<font><sz val='11'/><name val='Calibri'/></font>"
            + "<font><b/><sz val='11'/><name val='Calibri'/></font>"
            + "<font><b/><sz val='14'/><name val='Calibri'/></font>"
            + "</fonts>"
            + "<fills count='4'>"
            + "<fill><patternFill patternType='none'/></fill>"
            + "<fill><patternFill patternType='gray125'/></fill>"
            + "<fill><patternFill patternType='solid'><fgColor rgb='FFD9D9D9'/><bgColor indexed='64'/></patternFill></fill>"
            + "<fill><patternFill patternType='solid'><fgColor rgb='FFF2F2F2'/><bgColor indexed='64'/></patternFill></fill>"
            + "</fills>"
            + "<borders count='2'><border><left/><right/><top/><bottom/><diagonal/></border>" + BORDA + "</borders>"
            + "<cellStyleXfs count='1'><xf numFmtId='0' fontId='0' fillId='0' borderId='0'/></cellStyleXfs>"
            + "<cellXfs count='9'>"
            + "<xf numFmtId='0' fontId='0' fillId='0' borderId='0' xfId='0'/>"
            + "<xf numFmtId='0' fontId='2' fillId='0' borderId='0' xfId='0' applyFont='1'/>"
            + "<xf numFmtId='0' fontId='1' fillId='2' borderId='1' xfId='0' applyFont='1' applyFill='1' applyBorder='1'/>"
            + "<xf numFmtId='0' fontId='0' fillId='0' borderId='1' xfId='0' applyBorder='1'/>"
            + "<xf numFmtId='1' fontId='0' fillId='0' borderId='1' xfId='0' applyNumberFormat='1' applyBorder='1'/>"
            + "<xf numFmtId='164' fontId='0' fillId='0' borderId='1' xfId='0' applyNumberFormat='1' applyBorder='1'/>"
            + "<xf numFmtId='0' fontId='1' fillId='3' borderId='1' xfId='0' applyFont='1' applyFill='1' applyBorder='1'/>"
            + "<xf numFmtId='1' fontId='1' fillId='3' borderId='1' xfId='0' applyNumberFormat='1' applyFont='1' applyFill='1' applyBorder='1'/>"
            + "<xf numFmtId='164' fontId='1' fillId='3' borderId='1' xfId='0' applyNumberFormat='1' applyFont='1' applyFill='1' applyBorder='1'/>"
            + "</cellXfs>"
            + "<cellStyles count='1'><cellStyle name='Normal' xfId='0' builtinId='0'/></cellStyles>"
            + "</styleSheet>";

    private static byte[] gerarXlsx(Relatorio r) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(buffer, StandardCharsets.UTF_8)) {
            adicionarEntrada(zip, "[Content_Types].xml", CONTENT_TYPES);
            adicionarEntrada(zip, "_rels/.rels", RELS);
            adicionarEntrada(zip, "xl/workbook.xml", WORKBOOK);
            adicionarEntrada(zip, "xl/_rels/workbook.xml.rels", WORKBOOK_RELS);
            adicionarEntrada(zip, "xl/styles.xml", STYLES);
            adicionarEntrada(zip, "xl/worksheets/sheet1.xml", montarFolha(r));
        }
        return buffer.toByteArray();
    }

    private static void adicionarEntrada(ZipOutputStream zip, String nome, String conteudo) throws IOException {
        zip.putNextEntry(new ZipEntry(nome));
        zip.write(conteudo.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String montarFolha(Relatorio r) {
        int colunas = r.getColunas().length;
        StringBuilder sb = new StringBuilder(4096);
        sb.append(XML_DECL);
        sb.append("<worksheet xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main'>");

        sb.append("<cols>");
        for (int c = 0; c < colunas; c++) {
            sb.append("<col min='").append(c + 1).append("' max='").append(c + 1)
              .append("' width='").append(larguraColuna(r, c)).append("' customWidth='1'/>");
        }
        sb.append("</cols><sheetData>");

        int linha = 1;
        linha = linhaDeTexto(sb, linha, r.getTitulo(), E_TITULO);
        if (!r.getSubtitulo().isEmpty()) {
            linha = linhaDeTexto(sb, linha, r.getSubtitulo(), 0);
        }
        if (!r.getResumo().isEmpty()) {
            linha = linhaDeTexto(sb, linha, r.getResumo(), 0);
        }
        linha++; // linha em branco

        sb.append("<row r='").append(linha).append("'>");
        for (int c = 0; c < colunas; c++) {
            celulaTexto(sb, c, linha, r.getColunas()[c], E_CABECALHO);
        }
        sb.append("</row>");
        linha++;

        for (Object[] valores : r.getLinhas()) {
            linha = linhaDeDados(sb, linha, valores, false);
        }
        if (r.getLinhaTotal() != null) {
            linha = linhaDeDados(sb, linha, r.getLinhaTotal(), true);
        }

        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    private static int linhaDeTexto(StringBuilder sb, int linha, String texto, int estilo) {
        sb.append("<row r='").append(linha).append("'>");
        celulaTexto(sb, 0, linha, texto, estilo);
        sb.append("</row>");
        return linha + 1;
    }

    private static int linhaDeDados(StringBuilder sb, int linha, Object[] valores, boolean total) {
        sb.append("<row r='").append(linha).append("'>");
        for (int c = 0; c < valores.length; c++) {
            Object v = valores[c];
            if (v instanceof Double) {
                celulaNumero(sb, c, linha, BigDecimal.valueOf((Double) v).toPlainString(),
                        total ? E_TOTAL_MOEDA : E_MOEDA);
            } else if (v instanceof Number) {
                celulaNumero(sb, c, linha, v.toString(), total ? E_TOTAL_INTEIRO : E_INTEIRO);
            } else {
                celulaTexto(sb, c, linha, Relatorio.formatar(v), total ? E_TOTAL_TEXTO : E_TEXTO);
            }
        }
        sb.append("</row>");
        return linha + 1;
    }

    private static void celulaTexto(StringBuilder sb, int coluna, int linha, String texto, int estilo) {
        sb.append("<c r='").append(letraColuna(coluna)).append(linha)
          .append("' s='").append(estilo).append("' t='inlineStr'><is><t xml:space='preserve'>")
          .append(xml(texto)).append("</t></is></c>");
    }

    private static void celulaNumero(StringBuilder sb, int coluna, int linha, String numero, int estilo) {
        sb.append("<c r='").append(letraColuna(coluna)).append(linha)
          .append("' s='").append(estilo).append("'><v>").append(numero).append("</v></c>");
    }

    private static String letraColuna(int indice) {
        StringBuilder sb = new StringBuilder();
        for (int i = indice; i >= 0; i = i / 26 - 1) {
            sb.insert(0, (char) ('A' + i % 26));
        }
        return sb.toString();
    }

    private static int larguraColuna(Relatorio r, int coluna) {
        int maximo = r.getColunas()[coluna].length();
        for (Object[] valores : r.getLinhas()) {
            maximo = Math.max(maximo, Relatorio.formatar(valores[coluna]).length());
        }
        if (r.getLinhaTotal() != null) {
            maximo = Math.max(maximo, Relatorio.formatar(r.getLinhaTotal()[coluna]).length());
        }
        return Math.max(10, Math.min(maximo + 3, 45));
    }

    private static String xml(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&apos;");
                default -> {
                    if (ch >= 0x20 || ch == '\t' || ch == '\n' || ch == '\r') {
                        sb.append(ch);
                    }
                }
            }
        }
        return sb.toString();
    }

    // =====================================================================
    //  PDF
    // =====================================================================

    private static final Charset WIN_1252 = Charset.forName("windows-1252");

    // Larguras (em milésimos de em) da Helvetica para os caracteres ASCII 32..126
    private static final int[] LARGURAS_HELVETICA = {
        278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556,
        278, 278, 584, 584, 584, 556, 1015,
        667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833,
        722, 778, 667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611,
        278, 278, 278, 469, 556, 333,
        556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833,
        556, 556, 556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500,
        334, 260, 334, 584
    };

    private static final double MARGEM = 40;
    private static final double TAMANHO_FONTE = 9;
    private static final double ALTURA_LINHA = 20;
    private static final double ALTURA_CABECALHO = 22;
    private static final double PADDING = 5;
    private static final double LIMITE_INFERIOR = 50;

    private static byte[] gerarPdf(Relatorio r) throws IOException {
        boolean paisagem = r.getColunas().length > 5;
        double largPagina = paisagem ? 842 : 595;
        double altPagina = paisagem ? 595 : 842;
        double larguraTabela = largPagina - 2 * MARGEM;
        double topo = altPagina - MARGEM;
        int colunas = r.getColunas().length;

        // Linhas a desenhar: dados + total (se existir)
        List<Object[]> todas = new ArrayList<>(r.getLinhas());
        int indiceTotal = -1;
        if (r.getLinhaTotal() != null) {
            todas.add(r.getLinhaTotal());
            indiceTotal = todas.size() - 1;
        }

        double[] larguras = calcularLarguras(r, todas, indiceTotal, larguraTabela);

        boolean temSub = !r.getSubtitulo().isEmpty();
        boolean temRes = !r.getResumo().isEmpty();
        double topoTabela1 = topo - 16 - (temSub ? 18 : 0) - (temRes ? 14 : 0) - 14;
        double topoTabelaN = topo - 12 - 14;

        // Paginação: [inicio, fim) de cada página
        List<int[]> paginas = new ArrayList<>();
        int i = 0;
        boolean primeira = true;
        do {
            double topoTabela = primeira ? topoTabela1 : topoTabelaN;
            int capacidade = Math.max(1, (int) ((topoTabela - LIMITE_INFERIOR - ALTURA_CABECALHO) / ALTURA_LINHA));
            int fim = Math.min(todas.size(), i + capacidade);
            paginas.add(new int[]{i, fim});
            i = fim;
            primeira = false;
        } while (i < todas.size());

        String geradoEm = "Gerado em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Conteúdo de cada página
        List<String> conteudos = new ArrayList<>();
        for (int p = 0; p < paginas.size(); p++) {
            StringBuilder cs = new StringBuilder();
            double topoTabela;

            if (p == 0) {
                double y = topo - 16;
                texto(cs, "F2", 16, MARGEM, y, r.getTitulo());
                if (temSub) {
                    y -= 18;
                    texto(cs, "F1", 10, MARGEM, y, r.getSubtitulo());
                }
                if (temRes) {
                    y -= 14;
                    texto(cs, "F1", 10, MARGEM, y, r.getResumo());
                }
                topoTabela = topoTabela1;
            } else {
                texto(cs, "F2", 11, MARGEM, topo - 12, r.getTitulo() + " (continuação)");
                topoTabela = topoTabelaN;
            }

            cs.append("0.6 G 0.5 w\n");

            // Cabeçalho da tabela
            double x = MARGEM;
            double yBase = topoTabela - ALTURA_CABECALHO;
            for (int c = 0; c < colunas; c++) {
                retangulo(cs, x, yBase, larguras[c], ALTURA_CABECALHO, 0.82);
                celula(cs, r.getColunas()[c], x, yBase, larguras[c], ALTURA_CABECALHO,
                        true, r.isColunaNumerica(c));
                x += larguras[c];
            }

            // Linhas
            double yTopoLinha = yBase;
            for (int idx = paginas.get(p)[0]; idx < paginas.get(p)[1]; idx++) {
                boolean total = idx == indiceTotal;
                double cinza = total ? 0.90 : (idx % 2 == 0 ? 1.0 : 0.96);
                double yLinha = yTopoLinha - ALTURA_LINHA;
                x = MARGEM;
                Object[] valores = todas.get(idx);
                for (int c = 0; c < colunas; c++) {
                    retangulo(cs, x, yLinha, larguras[c], ALTURA_LINHA, cinza);
                    celula(cs, Relatorio.formatar(valores[c]), x, yLinha, larguras[c], ALTURA_LINHA,
                            total, r.isColunaNumerica(c));
                    x += larguras[c];
                }
                yTopoLinha = yLinha;
            }

            // Rodapé
            texto(cs, "F1", 8, MARGEM, 28, geradoEm);
            String numeroPagina = "Página " + (p + 1) + " de " + paginas.size();
            texto(cs, "F1", 8, largPagina - MARGEM - larguraTexto(numeroPagina, 8, false), 28, numeroPagina);

            conteudos.add(cs.toString());
        }

        return montarPdf(conteudos, largPagina, altPagina);
    }

    private static byte[] montarPdf(List<String> conteudos, double largPagina, double altPagina) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        int n = conteudos.size();

        escrever(out, "%PDF-1.4\n");
        out.write(new byte[]{'%', (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, '\n'});

        StringBuilder kids = new StringBuilder();
        for (int p = 0; p < n; p++) {
            kids.append(5 + 2 * p).append(" 0 R ");
        }

        objeto(out, offsets, "<< /Type /Catalog /Pages 2 0 R >>");
        objeto(out, offsets, "<< /Type /Pages /Kids [" + kids + "] /Count " + n + " >>");
        objeto(out, offsets, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        objeto(out, offsets, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");

        for (int p = 0; p < n; p++) {
            int contentObj = 6 + 2 * p;
            objeto(out, offsets, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + num(largPagina) + " " + num(altPagina)
                    + "] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + contentObj + " 0 R >>");
            String cs = conteudos.get(p);
            // cs só tem caracteres 0..255 (ver textoPdf), logo 1 char = 1 byte em ISO-8859-1
            objeto(out, offsets, "<< /Length " + cs.length() + " >>\nstream\n" + cs + "endstream");
        }

        int xrefPos = out.size();
        StringBuilder x = new StringBuilder();
        x.append("xref\n0 ").append(offsets.size() + 1).append("\n");
        x.append("0000000000 65535 f \n");
        for (int off : offsets) {
            x.append(String.format(Locale.ROOT, "%010d 00000 n \n", off));
        }
        x.append("trailer\n<< /Size ").append(offsets.size() + 1).append(" /Root 1 0 R >>\n");
        x.append("startxref\n").append(xrefPos).append("\n%%EOF\n");
        escrever(out, x.toString());

        return out.toByteArray();
    }

    private static void objeto(ByteArrayOutputStream out, List<Integer> offsets, String conteudo) throws IOException {
        offsets.add(out.size());
        escrever(out, offsets.size() + " 0 obj\n" + conteudo + "\nendobj\n");
    }

    private static void escrever(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    // ----- desenho -----

    private static void retangulo(StringBuilder cs, double x, double y, double w, double h, double cinza) {
        cs.append(num(cinza)).append(" g ")
          .append(num(x)).append(' ').append(num(y)).append(' ')
          .append(num(w)).append(' ').append(num(h)).append(" re B\n");
        cs.append("0 g\n");
    }

    private static void celula(StringBuilder cs, String valor, double x, double y, double w, double h,
                               boolean negrito, boolean direita) {
        String t = ajustar(valor, w - 2 * PADDING, negrito);
        double largura = larguraTexto(t, TAMANHO_FONTE, negrito);
        double xt = direita ? x + w - PADDING - largura : x + PADDING;
        double yt = y + (h - 6.5) / 2;
        texto(cs, negrito ? "F2" : "F1", TAMANHO_FONTE, xt, yt, t);
    }

    private static void texto(StringBuilder cs, String fonte, double tamanho, double x, double y, String s) {
        cs.append("BT /").append(fonte).append(' ').append(num(tamanho)).append(" Tf ")
          .append(num(x)).append(' ').append(num(y)).append(" Td (")
          .append(textoPdf(s)).append(") Tj ET\n");
    }

    /** Converte para Windows-1252 (1 char = 1 byte) e escapa os caracteres especiais do PDF. */
    private static String textoPdf(String s) {
        String latin = new String(s.getBytes(WIN_1252), StandardCharsets.ISO_8859_1);
        return latin.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private static String num(double v) {
        return String.format(Locale.ROOT, "%.2f", v);
    }

    // ----- medidas -----

    private static double larguraTexto(String s, double tamanho, boolean negrito) {
        double total = 0;
        for (char ch : s.toCharArray()) {
            total += (ch >= 32 && ch <= 126) ? LARGURAS_HELVETICA[ch - 32] : 600;
        }
        double largura = total * tamanho / 1000.0;
        return negrito ? largura * 1.08 : largura;
    }

    private static String ajustar(String s, double maximo, boolean negrito) {
        if (larguraTexto(s, TAMANHO_FONTE, negrito) <= maximo) {
            return s;
        }
        String base = s;
        while (base.length() > 1 && larguraTexto(base + "...", TAMANHO_FONTE, negrito) > maximo) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "...";
    }

    private static double[] calcularLarguras(Relatorio r, List<Object[]> linhas, int indiceTotal, double larguraTabela) {
        int colunas = r.getColunas().length;
        double[] medidas = new double[colunas];
        double soma = 0;
        for (int c = 0; c < colunas; c++) {
            double m = larguraTexto(r.getColunas()[c], TAMANHO_FONTE, true) + 2 * PADDING;
            for (int idx = 0; idx < linhas.size(); idx++) {
                String t = Relatorio.formatar(linhas.get(idx)[c]);
                m = Math.max(m, larguraTexto(t, TAMANHO_FONTE, idx == indiceTotal) + 2 * PADDING);
            }
            medidas[c] = Math.min(m, 220);
            soma += medidas[c];
        }
        double[] larguras = new double[colunas];
        for (int c = 0; c < colunas; c++) {
            larguras[c] = medidas[c] * larguraTabela / soma;
        }
        return larguras;
    }
}