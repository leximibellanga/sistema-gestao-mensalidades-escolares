package App.view.dashboard;

import App.model.Pagamento;
import App.model.ResumoDashboard;
import App.service.DashboardService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PainelDashboard extends JPanel {

    private static final Color COR_FUNDO = new Color(238, 238, 238);
    private static final Color AZUL = new Color(51, 102, 153);
    private static final Color VERDE = new Color(30, 130, 60);
    private static final Color LARANJA = new Color(170, 110, 0);
    private static final Color VERMELHO = new Color(190, 40, 30);
    private static final Locale PT = Locale.forLanguageTag("pt-PT");

    private final DashboardService service = new DashboardService();

    private final JLabel lblTitulo = new JLabel("Painel Geral");
    private final CardResumo cardAlunos = new CardResumo(AZUL);
    private final CardResumo cardArrecadado = new CardResumo(VERDE);
    private final CardResumo cardPendente = new CardResumo(LARANJA);
    private final CardResumo cardAtraso = new CardResumo(VERMELHO);
    private final GraficoBarras grafico = new GraficoBarras();
    private final ModeloPagamentos modeloPagamentos = new ModeloPagamentos();
    private final JLabel lblTituloPagamentos = new JLabel("Últimos pagamentos");

    public PainelDashboard() {
        setLayout(new BorderLayout());
        setBackground(COR_FUNDO);

        add(criarTopo(), BorderLayout.NORTH);
        add(criarCorpo(), BorderLayout.CENTER);

        carregar();

        // Os números mudam nos outros ecrãs: actualiza sempre que o painel volta a aparecer
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                carregar();
            }
        });
    }

    // ---------- Construção da interface ----------
    private JPanel criarTopo() {
        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.setFocusPainted(false);
        btnAtualizar.addActionListener(e -> carregar());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        barra.setBackground(new Color(214, 214, 214));
        barra.add(btnAtualizar);

        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20, 20, 12, 20));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(barra, BorderLayout.NORTH);
        topo.add(lblTitulo, BorderLayout.CENTER);
        return topo;
    }

    private JPanel criarCorpo() {
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.add(cardAlunos);
        cards.add(cardArrecadado);
        cards.add(cardPendente);
        cards.add(cardAtraso);

        JPanel baixo = new JPanel(new BorderLayout(16, 0));
        baixo.setOpaque(false);
        baixo.add(caixa(new JLabel("Arrecadado nos últimos 6 meses (MT)"), grafico), BorderLayout.WEST);
        baixo.add(caixa(lblTituloPagamentos, criarTabelaPagamentos()), BorderLayout.CENTER);

        JPanel corpo = new JPanel(new BorderLayout(0, 20));
        corpo.setOpaque(false);
        corpo.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        corpo.add(cards, BorderLayout.NORTH);
        corpo.add(baixo, BorderLayout.CENTER);
        return corpo;
    }

    private JPanel caixa(JLabel titulo, JComponent conteudo) {
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel caixa = new JPanel(new BorderLayout());
        caixa.setBackground(Color.WHITE);
        caixa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(14, 16, 16, 16)));
        caixa.add(titulo, BorderLayout.NORTH);
        caixa.add(conteudo, BorderLayout.CENTER);
        return caixa;
    }

    private JScrollPane criarTabelaPagamentos() {
        JTable tabela = new JTable(modeloPagamentos);
        tabela.setRowHeight(28);
        tabela.setShowVerticalLines(false);
        tabela.setRowSelectionAllowed(false);
        tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        // Linhas alternadas e valor à direita
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean selecionada, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, false, false, linha, coluna);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                setHorizontalAlignment(coluna == ModeloPagamentos.COL_VALOR
                        ? SwingConstants.RIGHT : SwingConstants.LEFT);
                setBackground(linha % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                return this;
            }
        });

        int[] larguras = {92, 140, 66, 80, 82};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    // ---------- Dados ----------
    private void carregar() {
        try {
            ResumoDashboard r = service.carregar();
            String mes = nomeDoMes(r.mes);

            lblTitulo.setText("Painel Geral — " + mes + " de " + r.mes.getYear());

            cardAlunos.definir("Alunos matriculados", String.valueOf(r.totalAlunos),
                    "em " + quantidade(r.totalTurmas, "turma", "turmas"));
            cardArrecadado.definir("Arrecadado em " + mes, moeda(r.pagasValor),
                    quantidade(r.pagasQuantidade, "mensalidade paga", "mensalidades pagas"));
            cardPendente.definir("A receber em " + mes, moeda(r.pendentesValor),
                    quantidade(r.pendentesQuantidade, "mensalidade pendente", "mensalidades pendentes"));
            cardAtraso.definir("Em atraso (total)", moeda(r.atrasadasValor),
                    quantidade(r.atrasadasQuantidade, "mensalidade", "mensalidades"));

            grafico.setDados(r.arrecadadoPorMes);
            modeloPagamentos.setDados(r.ultimosPagamentos);
            lblTituloPagamentos.setText(r.ultimosPagamentos.isEmpty()
                    ? "Últimos pagamentos — ainda não há pagamentos registados"
                    : "Últimos pagamentos");
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar o painel:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String nomeDoMes(LocalDate mes) {
        String nome = mes.getMonth().getDisplayName(TextStyle.FULL, PT);
        return Character.toUpperCase(nome.charAt(0)) + nome.substring(1);
    }

    private static String moeda(double valor) {
        return String.format(Locale.US, "%.2f MT", valor);
    }

    private static String quantidade(int n, String singular, String plural) {
        return n + " " + (n == 1 ? singular : plural);
    }

    // ---------- Componentes internos ----------
    /**
     * Bloco de resumo: título, valor grande e detalhe, com uma faixa de cor no
     * topo.
     */
    private static class CardResumo extends JPanel {

        private final JLabel lblTitulo = new JLabel(" ");
        private final JLabel lblValor = new JLabel("-");
        private final JLabel lblDetalhe = new JLabel(" ");

        CardResumo(Color cor) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(5, 0, 0, 0, cor),
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(200, 200, 200)),
                            BorderFactory.createEmptyBorder(12, 16, 14, 16))));

            lblTitulo.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblTitulo.setForeground(new Color(90, 90, 90));
            lblValor.setFont(new Font("SansSerif", Font.BOLD, 20));
            lblValor.setForeground(cor);
            lblDetalhe.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lblDetalhe.setForeground(new Color(110, 110, 110));

            lblTitulo.setAlignmentX(LEFT_ALIGNMENT);
            lblValor.setAlignmentX(LEFT_ALIGNMENT);
            lblDetalhe.setAlignmentX(LEFT_ALIGNMENT);

            add(lblTitulo);
            add(Box.createVerticalStrut(6));
            add(lblValor);
            add(Box.createVerticalStrut(4));
            add(lblDetalhe);
        }

        void definir(String titulo, String valor, String detalhe) {
            lblTitulo.setText(titulo);
            lblValor.setText(valor);
            lblDetalhe.setText(detalhe);
        }
    }

    /**
     * TableModel da lista dos últimos pagamentos.
     */
    private static class ModeloPagamentos extends AbstractTableModel {

        static final int COL_VALOR = 3;

        private static final String[] COLUNAS = {"Data", "Aluno", "Mês ref.", "Valor (MT)", "Método"};
        private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MM/yyyy");

        private List<Pagamento> pagamentos = new ArrayList<>();

        void setDados(List<Pagamento> lista) {
            this.pagamentos = lista;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return pagamentos.size();
        }

        @Override
        public int getColumnCount() {
            return COLUNAS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUNAS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Pagamento p = pagamentos.get(rowIndex);
            return switch (columnIndex) {
                case 0 ->
                    p.getDataPagamento().format(FORMATO_DATA);
                case 1 ->
                    p.getMensalidade().getAluno().getNome();
                case 2 ->
                    p.getMensalidade().getMesReferencia().format(FORMATO_MES);
                case 3 ->
                    String.format(Locale.US, "%.2f", p.getValorPago());
                case 4 ->
                    p.getMetodo() == null ? "" : p.getMetodo();
                default ->
                    null;
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }
}
