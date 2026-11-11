package App.view.mensalidades;

import App.model.Mensalidade;
import App.model.Pagamento;
import App.model.StatusMensalidade;
import App.service.MensalidadeService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PainelMensalidades extends JPanel {

    private static final Color COR_FUNDO = new Color(238, 238, 238);

    // Mais recentes primeiro; dentro do mês, por nome do aluno
    private static final Comparator<Mensalidade> ORDEM = Comparator
            .comparing(Mensalidade::getMesReferencia).reversed()
            .thenComparing(m -> m.getAluno().getNome());

    private final MensalidadeService service = new MensalidadeService();
    private final TabelaMensalidadesModel tabelaModel = new TabelaMensalidadesModel();
    private final JTable tblMensalidades = new JTable(tabelaModel);

    private final JComboBox<ItemMes> cmbMes = new JComboBox<>();
    private final JComboBox<String> cmbStatus = new JComboBox<>(
            new String[]{"Todas", "Pendentes", "Pagas", "Atrasadas"});
    private final JLabel lblResumo = new JLabel(" ");

    public PainelMensalidades() {
        setLayout(new BorderLayout());
        setBackground(COR_FUNDO);

        popularMeses();

        add(criarTopo(), BorderLayout.NORTH);
        add(criarTabela(), BorderLayout.CENTER);

        // Só depois de preencher os combos, para não disparar carregamentos a mais
        cmbMes.addActionListener(e -> carregarTabela());
        cmbStatus.addActionListener(e -> carregarTabela());

        carregarTabela();
    }

    // ---------- Construção da interface ----------

    private void popularMeses() {
        cmbMes.addItem(new ItemMes(null)); // "Todos os meses"

        LocalDate atual = LocalDate.now().withDayOfMonth(1);
        ItemMes itemAtual = null;
        for (int i = 2; i >= -12; i--) { // 2 meses à frente, 12 para trás
            ItemMes item = new ItemMes(atual.plusMonths(i));
            cmbMes.addItem(item);
            if (i == 0) {
                itemAtual = item;
            }
        }
        cmbMes.setSelectedItem(itemAtual);
    }

    private JPanel criarTopo() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        barra.setBackground(new Color(214, 214, 214));
        barra.add(criarBotao("Gerar Mensalidades", e -> gerarMensalidades()));
        barra.add(criarBotao("Atualizar Atrasados", e -> atualizarAtrasados()));
        barra.add(criarBotao("Registar Pagamento", e -> registarPagamento()));
        barra.add(criarBotao("Atualizar Lista", e -> carregarTabela()));

        JLabel titulo = new JLabel("Gestão de Mensalidades");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        filtros.add(new JLabel("Mês:"));
        filtros.add(cmbMes);
        filtros.add(Box.createHorizontalStrut(12));
        filtros.add(new JLabel("Status:"));
        filtros.add(cmbStatus);

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

    private JPanel criarTabela() {
        tblMensalidades.setRowHeight(30);
        tblMensalidades.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblMensalidades.setShowVerticalLines(false);
        tblMensalidades.setFillsViewportHeight(true);
        tblMensalidades.getTableHeader().setReorderingAllowed(false);
        tblMensalidades.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        // Linhas alternadas e cor no status
        tblMensalidades.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean selecionada, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, selecionada, false, linha, coluna);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (!selecionada) {
                    setBackground(linha % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                    if (coluna == TabelaMensalidadesModel.COL_STATUS) {
                        Mensalidade m = tabelaModel.getMensalidadeNaLinha(t.convertRowIndexToModel(linha));
                        setForeground(corDoStatus(m.getStatus()));
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                }
                return this;
            }
        });

        int[] larguras = {110, 230, 120, 90, 120, 100};
        for (int i = 0; i < larguras.length; i++) {
            tblMensalidades.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }

        // Duplo clique abre o registo de pagamento
        tblMensalidades.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblMensalidades.getSelectedRow() >= 0) {
                    registarPagamento();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblMensalidades);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(170, 170, 170)));
        scroll.getViewport().setBackground(Color.WHITE);

        lblResumo.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 20, 16, 20));
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

    private Color corDoStatus(StatusMensalidade status) {
        return switch (status) {
            case PAGO -> new Color(30, 130, 60);
            case ATRASADO -> new Color(190, 40, 30);
            case PENDENTE -> new Color(170, 110, 0);
        };
    }

    // ---------- Ações ----------

    private void carregarTabela() {
        try {
            ItemMes item = (ItemMes) cmbMes.getSelectedItem();
            List<Mensalidade> lista = (item == null || item.mes == null)
                    ? service.listarTodas()
                    : service.listarPorMes(item.mes);

            StatusMensalidade filtro = statusSelecionado();
            List<Mensalidade> filtradas = lista.stream()
                    .filter(m -> filtro == null || m.getStatus() == filtro)
                    .sorted(ORDEM)
                    .collect(Collectors.toList());

            tabelaModel.atualizarDados(filtradas);

            double total = filtradas.stream().mapToDouble(Mensalidade::getValor).sum();
            lblResumo.setText(String.format(Locale.US,
                    "%d mensalidade(s)   |   Total: %.2f MT", filtradas.size(), total));
        } catch (RuntimeException e) {
            erro("Erro ao carregar as mensalidades:\n" + e.getMessage());
        }
    }

    private void gerarMensalidades() {
        ItemMes item = (ItemMes) cmbMes.getSelectedItem();
        if (item == null || item.mes == null) {
            aviso("Selecione um mês concreto para gerar as mensalidades.");
            return;
        }
        int opcao = JOptionPane.showConfirmDialog(this,
                "Gerar as mensalidades de " + item + " para todos os alunos?",
                "Gerar mensalidades", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcao != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int geradas = service.gerarMensalidadesDoMes(item.mes);
            carregarTabela();
            String msg = geradas == 0
                    ? "Nenhuma mensalidade nova: todos os alunos já tinham mensalidade neste mês."
                    : geradas + " mensalidade(s) gerada(s) para " + item + ".";
            JOptionPane.showMessageDialog(this, msg, "Gerar mensalidades",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException e) {
            erro("Não foi possível gerar as mensalidades:\n" + e.getMessage());
        }
    }

    private void atualizarAtrasados() {
        try {
            int marcadas = service.atualizarStatusAtrasados();
            carregarTabela();
            String msg = marcadas == 0
                    ? "Nenhuma mensalidade passou a atrasada."
                    : marcadas + " mensalidade(s) marcada(s) como atrasada(s).";
            JOptionPane.showMessageDialog(this, msg, "Atualizar atrasados",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException e) {
            erro("Não foi possível atualizar os atrasados:\n" + e.getMessage());
        }
    }

    private void registarPagamento() {
        Mensalidade m = mensalidadeSelecionada();
        if (m == null) {
            return;
        }
        if (m.getStatus() == StatusMensalidade.PAGO) {
            aviso("Esta mensalidade já está paga.");
            return;
        }
        DialogRegistrarPagamento dialog = new DialogRegistrarPagamento(janelaPai(), m);
        dialog.setVisible(true);

        Pagamento pagamento = dialog.getPagamento();
        if (pagamento != null) {
            carregarTabela();
            mostrarRecibo(pagamento);
        }
    }

    private void mostrarRecibo(Pagamento pagamento) {
        JTextArea area = new JTextArea(pagamento.gerarRecibo());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JOptionPane.showMessageDialog(this, area, "Recibo de pagamento",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------- Auxiliares ----------

    private StatusMensalidade statusSelecionado() {
        return switch (cmbStatus.getSelectedIndex()) {
            case 1 -> StatusMensalidade.PENDENTE;
            case 2 -> StatusMensalidade.PAGO;
            case 3 -> StatusMensalidade.ATRASADO;
            default -> null; // "Todas"
        };
    }

    private Mensalidade mensalidadeSelecionada() {
        int linha = tblMensalidades.getSelectedRow();
        if (linha < 0) {
            aviso("Selecione uma mensalidade na tabela.");
            return null;
        }
        return tabelaModel.getMensalidadeNaLinha(tblMensalidades.convertRowIndexToModel(linha));
    }

    private Frame janelaPai() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }

    private void aviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void erro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    /** Item do combo de meses; mes == null significa "Todos os meses". */
    private static class ItemMes {
        private static final String[] NOMES = {
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        };

        final LocalDate mes;

        ItemMes(LocalDate mes) {
            this.mes = mes;
        }

        @Override
        public String toString() {
            return mes == null
                    ? "Todos os meses"
                    : NOMES[mes.getMonthValue() - 1] + " / " + mes.getYear();
        }
    }
}