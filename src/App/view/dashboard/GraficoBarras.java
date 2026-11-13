package App.view.dashboard;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Gráfico de barras desenhado à mão (sem bibliotecas): um valor por mês.
 */
public class GraficoBarras extends JPanel {

    private static final Locale PT = Locale.forLanguageTag("pt-PT");
    private static final Color COR_BARRA = new Color(150, 180, 210);
    private static final Color COR_BARRA_ATUAL = new Color(51, 102, 153);
    private static final Color COR_TEXTO = new Color(70, 70, 70);

    private Map<LocalDate, Double> dados = new LinkedHashMap<>();

    public GraficoBarras() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(300, 240));
    }

    /**
     * O último mês da série é o destacado (o mês actual).
     */
    public void setDados(Map<LocalDate, Double> dados) {
        this.dados = dados;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        double maximo = dados.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        if (dados.isEmpty() || maximo <= 0) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.setColor(new Color(130, 130, 130));
            FontMetrics fm = g2.getFontMetrics();
            String[] linhas = {"Ainda não há mensalidades", "pagas neste período."};
            int y = getHeight() / 2 - fm.getHeight() / 2;
            for (String linha : linhas) {
                g2.drawString(linha, (getWidth() - fm.stringWidth(linha)) / 2, y);
                y += fm.getHeight();
            }
            g2.dispose();
            return;
        }

        int margemLateral = 8;
        int margemTopo = 26;   // espaço para o valor por cima da barra mais alta
        int margemBase = 28;   // espaço para o nome do mês
        int areaLargura = getWidth() - 2 * margemLateral;
        int areaAltura = getHeight() - margemTopo - margemBase;
        int baseY = margemTopo + areaAltura;

        g2.setColor(new Color(190, 190, 190));
        g2.drawLine(margemLateral, baseY, margemLateral + areaLargura, baseY);

        int n = dados.size();
        int slot = areaLargura / n;
        int larguraBarra = (int) (slot * 0.55);

        int i = 0;
        for (Map.Entry<LocalDate, Double> entrada : dados.entrySet()) {
            boolean atual = i == n - 1;
            double valor = entrada.getValue();
            int alturaBarra = valor > 0 ? Math.max(3, (int) Math.round(valor / maximo * areaAltura)) : 0;
            int x = margemLateral + i * slot + (slot - larguraBarra) / 2;
            int y = baseY - alturaBarra;

            g2.setColor(atual ? COR_BARRA_ATUAL : COR_BARRA);
            g2.fillRect(x, y, larguraBarra, alturaBarra);

            g2.setColor(COR_TEXTO);
            g2.setFont(new Font("SansSerif", atual ? Font.BOLD : Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            String textoValor = String.format(Locale.US, "%.0f", valor);
            g2.drawString(textoValor, x + (larguraBarra - fm.stringWidth(textoValor)) / 2, y - 6);

            String mes = abreviatura(entrada.getKey());
            g2.drawString(mes, x + (larguraBarra - fm.stringWidth(mes)) / 2, baseY + 18);
            i++;
        }
        g2.dispose();
    }

    /**
     * "novembro" -> "Nov"
     */
    private String abreviatura(LocalDate mes) {
        String nome = mes.getMonth().getDisplayName(TextStyle.FULL, PT);
        return Character.toUpperCase(nome.charAt(0)) + nome.substring(1, 3);
    }
}
