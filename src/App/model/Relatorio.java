package App.model;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Resultado de um relatório: título, colunas, linhas de dados e (opcional) linha de totais.
 * Valores Double são tratados como dinheiro (MT); Integer/Long como contagens.
 */
public class Relatorio {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String titulo;
    private final String subtitulo;
    private final String[] colunas;
    private final List<Object[]> linhas = new ArrayList<>();
    private Object[] linhaTotal;
    private String resumo = "";

    public Relatorio(String titulo, String subtitulo, String[] colunas) {
        this.titulo = titulo;
        this.subtitulo = subtitulo == null ? "" : subtitulo;
        this.colunas = colunas;
    }

    public void adicionarLinha(Object... valores) {
        validar(valores);
        linhas.add(valores);
    }

    public void setLinhaTotal(Object... valores) {
        validar(valores);
        this.linhaTotal = valores;
    }

    private void validar(Object[] valores) {
        if (valores.length != colunas.length) {
            throw new IllegalArgumentException("A linha tem " + valores.length
                    + " valores, mas o relatório tem " + colunas.length + " colunas.");
        }
    }

    public String getTitulo() {
        return titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public String[] getColunas() {
        return colunas;
    }

    public List<Object[]> getLinhas() {
        return Collections.unmodifiableList(linhas);
    }

    public Object[] getLinhaTotal() {
        return linhaTotal;
    }

    public String getResumo() {
        return resumo;
    }

    public void setResumo(String resumo) {
        this.resumo = resumo == null ? "" : resumo;
    }

    public boolean temDados() {
        return !linhas.isEmpty();
    }

    /** Uma coluna é numérica (alinhada à direita) se o seu valor for um Number. */
    public boolean isColunaNumerica(int coluna) {
        if (!linhas.isEmpty()) {
            return linhas.get(0)[coluna] instanceof Number;
        }
        return linhaTotal != null && linhaTotal[coluna] instanceof Number;
    }

    /** Texto de um valor tal como aparece no ecrã, no PDF e na impressão. */
    public static String formatar(Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof Double) {
            return String.format(Locale.US, "%.2f MT", (Double) valor);
        }
        if (valor instanceof LocalDate) {
            return ((LocalDate) valor).format(FORMATO_DATA);
        }
        return valor.toString();
    }

    /** Nome sugerido para ficheiros exportados, sem extensão. */
    public String nomeBaseFicheiro() {
        String base = titulo + (subtitulo.isEmpty() ? "" : " " + subtitulo);
        String slug = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (slug.length() > 60) {
            slug = slug.substring(0, 60).replaceAll("_+$", "");
        }
        return "relatorio_" + slug + "_" + LocalDate.now();
    }
}