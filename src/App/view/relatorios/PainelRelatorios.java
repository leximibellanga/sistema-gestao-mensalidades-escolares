package App.view.relatorios;

import App.model.Aluno;
import App.model.Relatorio;
import App.model.Turma;
import App.service.AlunoService;
import App.service.RelatorioService;
import App.service.TurmaService;
import App.util.ExportadorRelatorio;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;

public class PainelRelatorios extends JPanel {

    private static final Color COR_FUNDO = new Color(238, 238, 238);
    private static final String TODAS_AS_TURMAS = "Todas as turmas";
    private static final String[] TIPOS = {
        "Mensalidades em atraso",
        "Resumo por turma (mês)",
        "Arrecadado por mês (ano)",
        "Histórico de pagamentos (aluno)"
    };

    private final RelatorioService relatorioService = new RelatorioService();
    private final TurmaService turmaService = new TurmaService();
    private final AlunoService alunoService = new AlunoService();

    private final TabelaRelatorioModel tabelaModel = new TabelaRelatorioModel();
    private final JTable tblRelatorio = new JTable(tabelaModel);

    private final JComboBox<String> cmbTipo = new JComboBox<>(TIPOS);
    private final CardLayout cardFiltros = new CardLayout();
    private final JPanel pnlFiltros = new JPanel(cardFiltros);
    private final JComboBox<Object> cmbTurma = new JComboBox<>();
    private final JComboBox<ItemMes> cmbMes = new JComboBox<>();
    private final JComboBox<Integer> cmbAno = new JComboBox<>();
    private final JComboBox<Aluno> cmbAluno = new JComboBox<>();

    private final JLabel lblTituloRelatorio = new JLabel(" ");
    private final JLabel lblResumo = new JLabel("Escolha o tipo de relatório e clique em \"Gerar Relatório\".");

    private JButton btnExcel;
    private JButton btnPdf;
    private JButton btnImprimir;

    private Relatorio relatorioAtual;

    public PainelRelatorios() {
        setLayout(new BorderLayout());
        setBackground(COR_FUNDO);

        popularMeses();
        popularAnos();
        recarregarListas();

        add(criarTopo(), BorderLayout.NORTH);
        add(criarTabela(), BorderLayout.CENTER);

        cmbTipo.addActionListener(e -> cardFiltros.show(pnlFiltros, String.valueOf(cmbTipo.getSelectedIndex())));
        habilitarExportacao(false);

        // Turmas e alunos podem ter mudado desde a última vez: recarrega ao voltar a este ecrã
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                recarregarListas();
            }
        });
    }

    // ---------- Construção da interface ----------

    private JPanel criarTopo() {
        btnExcel = criarBotao("Exportar Excel", e -> exportar(false));
        btnPdf = criarBotao("Exportar PDF", e -> exportar(true));
        btnImprimir = criarBotao("Imprimir", e -> imprimir());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        barra.setBackground(new Color(214, 214, 214));
        barra.add(criarBotao("Gerar Relatório", e -> gerar()));
        barra.add(btnExcel);
        barra.add(btnPdf);
        barra.add(btnImprimir);

        JLabel titulo = new JLabel("Relatórios");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));

        pnlFiltros.setOpaque(false);
        pnlFiltros.add(comRotulo("Turma:", cmbTurma), "0");
        pnlFiltros.add(comRotulo("Mês:", cmbMes), "1");
        pnlFiltros.add(comRotulo("Ano:", cmbAno), "2");
        pnlFiltros.add(comRotulo("Aluno:", cmbAluno), "3");

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        filtros.add(new JLabel("Tipo:"));
        filtros.add(cmbTipo);
        filtros.add(Box.createHorizontalStrut(16));
        filtros.add(pnlFiltros);

        JPanel cabecalho = new JPanel(new BorderLayout(0, 12));
        cabecalho.setOpaque(false);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(20, 20, 12, 20));
        cabecalho.add(titulo, BorderLayout.NORTH);
        cabecalho.add(filtros, BorderLayout.CENTER);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(barra, BorderLayout.NORTH);
        topo.add(cabecalho, BorderLayout.CENTER);
        return topo;
    }

    private JPanel comRotulo(String rotulo, JComboBox<?> combo) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        p.add(new JLabel(rotulo));
        p.add(combo);
        return p;
    }

    private JPanel criarTabela() {
        tblRelatorio.setRowHeight(30);
        tblRelatorio.setShowVerticalLines(false);
        tblRelatorio.setFillsViewportHeight(true);
        tblRelatorio.setRowSelectionAllowed(false);
        tblRelatorio.getTableHeader().setReorderingAllowed(false);
        tblRelatorio.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        // Zebra, números à direita e linha de total em negrito
        tblRelatorio.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean selecionada, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, false, false, linha, coluna);
                boolean total = tabelaModel.isLinhaTotal(linha);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                setHorizontalAlignment(tabelaModel.isColunaNumerica(t.convertColumnIndexToModel(coluna))
                        ? SwingConstants.RIGHT : SwingConstants.LEFT);
                setFont(getFont().deriveFont(total ? Font.BOLD : Font.PLAIN));
                setBackground(total ? new Color(222, 222, 222)
                        : (linha % 2 == 0 ? Color.WHITE : new Color(240, 240, 240)));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(tblRelatorio);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(170, 170, 170)));
        scroll.getViewport().setBackground(Color.WHITE);

        lblTituloRelatorio.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblResumo.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 20, 16, 20));
        wrapper.add(lblTituloRelatorio, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);
        wrapper.add(lblResumo, BorderLayout.SOUTH);
        return wrapper;
    }

    private JButton criarBotao(String texto, ActionListener acao) {
        JButton btn = new JButton(texto);
        btn.setFocusPainted(false);
        btn.addActionListener(acao);
        return btn;
    }

    // ---------- Filtros ----------

    private void popularMeses() {
        LocalDate atual = LocalDate.now().withDayOfMonth(1);
        ItemMes itemAtual = null;
        for (int i = 2; i >= -24; i--) { // 2 meses à frente, 24 para trás
            ItemMes item = new ItemMes(atual.plusMonths(i));
            cmbMes.addItem(item);
            if (i == 0) {
                itemAtual = item;
            }
        }
        cmbMes.setSelectedItem(itemAtual);
    }

    private void popularAnos() {
        int anoAtual = LocalDate.now().getYear();
        for (int ano = anoAtual + 1; ano >= anoAtual - 5; ano--) {
            cmbAno.addItem(ano);
        }
        cmbAno.setSelectedItem(anoAtual);
    }

    /** Recarrega turmas e alunos mantendo, se possível, a selecção actual. */
    private void recarregarListas() {
        try {
            Turma turmaAnterior = turmaSelecionada();
            Aluno alunoAnterior = (Aluno) cmbAluno.getSelectedItem();

            cmbTurma.removeAllItems();
            cmbTurma.addItem(TODAS_AS_TURMAS);
            Object turmaParaSelecionar = TODAS_AS_TURMAS;
            for (Turma t : turmaService.listarTodas()) {
                cmbTurma.addItem(t);
                if (turmaAnterior != null && t.getId() == turmaAnterior.getId()) {
                    turmaParaSelecionar = t;
                }
            }
            cmbTurma.setSelectedItem(turmaParaSelecionar);

            cmbAluno.removeAllItems();
            Aluno alunoParaSelecionar = null;
            for (Aluno a : alunoService.listarTodos()) {
                cmbAluno.addItem(a);
                if (alunoAnterior != null && a.getId() == alunoAnterior.getId()) {
                    alunoParaSelecionar = a;
                }
            }
            if (alunoParaSelecionar != null) {
                cmbAluno.setSelectedItem(alunoParaSelecionar);
            }
        } catch (RuntimeException e) {
            erro("Erro ao carregar os filtros (turmas e alunos):\n" + e.getMessage());
        }
    }

    private Turma turmaSelecionada() {
        Object item = cmbTurma.getSelectedItem();
        return item instanceof Turma ? (Turma) item : null; // null = todas as turmas
    }

    private LocalDate mesSelecionado() {
        return ((ItemMes) cmbMes.getSelectedItem()).mes;
    }

    private int anoSelecionado() {
        return (Integer) cmbAno.getSelectedItem();
    }

    private Aluno alunoSelecionado() {
        Aluno aluno = (Aluno) cmbAluno.getSelectedItem();
        if (aluno == null) {
            throw new IllegalArgumentException("Não há alunos registados para gerar o histórico.");
        }
        return aluno;
    }

    // ---------- Ações ----------

    private void gerar() {
        try {
            Relatorio r = switch (cmbTipo.getSelectedIndex()) {
                case 0 -> relatorioService.mensalidadesEmAtraso(turmaSelecionada());
                case 1 -> relatorioService.resumoPorTurma(mesSelecionado());
                case 2 -> relatorioService.arrecadadoPorMes(anoSelecionado());
                default -> relatorioService.historicoPagamentos(alunoSelecionado());
            };
            mostrar(r);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            erro("Não foi possível gerar o relatório:\n" + e.getMessage());
        }
    }

    private void mostrar(Relatorio r) {
        relatorioAtual = r;
        tabelaModel.setRelatorio(r);
        ajustarLarguras();

        lblTituloRelatorio.setText(r.getTitulo()
                + (r.getSubtitulo().isEmpty() ? "" : "  —  " + r.getSubtitulo()));
        lblResumo.setText(r.temDados() ? r.getResumo() : "Nenhum registo encontrado para os filtros escolhidos.");
        habilitarExportacao(r.temDados());
    }

    private void exportar(boolean pdf) {
        if (relatorioAtual == null) {
            return;
        }
        String extensao = pdf ? "pdf" : "xlsx";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(pdf ? "Exportar para PDF" : "Exportar para Excel");
        chooser.setSelectedFile(new File(relatorioAtual.nomeBaseFicheiro() + "." + extensao));
        chooser.setFileFilter(new FileNameExtensionFilter(
                pdf ? "Documento PDF (*.pdf)" : "Folha de Excel (*.xlsx)", extensao));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = chooser.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith("." + extensao)) {
            destino = new File(destino.getParentFile(), destino.getName() + "." + extensao);
        }
        if (destino.exists()) {
            int opcao = JOptionPane.showConfirmDialog(this,
                    "O ficheiro \"" + destino.getName() + "\" já existe. Substituir?",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opcao != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            if (pdf) {
                ExportadorRelatorio.paraPdf(relatorioAtual, destino);
            } else {
                ExportadorRelatorio.paraXlsx(relatorioAtual, destino);
            }
        } catch (IOException e) {
            erro("Não foi possível gravar o ficheiro.\n"
               + "Se ele estiver aberto noutro programa, feche-o e tente de novo.\n\n" + e.getMessage());
            return;
        }

        int abrir = JOptionPane.showConfirmDialog(this,
                "Relatório gravado em:\n" + destino.getAbsolutePath() + "\n\nDeseja abrir o ficheiro agora?",
                "Exportação concluída", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (abrir == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(destino);
            } catch (IOException | RuntimeException e) {
                JOptionPane.showMessageDialog(this, "Não foi possível abrir o ficheiro automaticamente.",
                        "Atenção", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void imprimir() {
        if (relatorioAtual == null) {
            return;
        }
        String cabecalho = relatorioAtual.getTitulo()
                + (relatorioAtual.getSubtitulo().isEmpty() ? "" : " - " + relatorioAtual.getSubtitulo());
        try {
            tblRelatorio.print(JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat(semFormato(cabecalho)),
                    new MessageFormat("Página {0}"));
        } catch (PrinterException e) {
            erro("Não foi possível imprimir:\n" + e.getMessage());
        }
    }

    // ---------- Auxiliares ----------

    private void habilitarExportacao(boolean ativo) {
        btnExcel.setEnabled(ativo);
        btnPdf.setEnabled(ativo);
        btnImprimir.setEnabled(ativo);
    }

    /** Ajusta a largura de cada coluna ao conteúdo (o espaço que sobra é repartido). */
    private void ajustarLarguras() {
        FontMetrics fmCabecalho = tblRelatorio.getFontMetrics(tblRelatorio.getTableHeader().getFont());
        FontMetrics fmCelula = tblRelatorio.getFontMetrics(tblRelatorio.getFont().deriveFont(Font.BOLD));
        for (int c = 0; c < tblRelatorio.getColumnCount(); c++) {
            int largura = fmCabecalho.stringWidth(tabelaModel.getColumnName(c)) + 32;
            for (int l = 0; l < tabelaModel.getRowCount(); l++) {
                largura = Math.max(largura, fmCelula.stringWidth(String.valueOf(tabelaModel.getValueAt(l, c))) + 24);
            }
            tblRelatorio.getColumnModel().getColumn(c).setPreferredWidth(Math.min(largura, 400));
        }
    }

    /** Escapa os caracteres que o MessageFormat interpretaria como padrões. */
    private String semFormato(String s) {
        return s.replace("'", "''").replace("{", "'{'");
    }

    private void erro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    /** Item do combo de meses. */
    private static class ItemMes {
        final LocalDate mes;

        ItemMes(LocalDate mes) {
            this.mes = mes;
        }

        @Override
        public String toString() {
            return RelatorioService.NOMES_MESES[mes.getMonthValue() - 1] + " / " + mes.getYear();
        }
    }
}