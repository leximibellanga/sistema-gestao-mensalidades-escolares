package App.view.turmas;

import App.model.Turma;
import App.service.TurmaService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
import java.util.List;

public class PainelTurmas extends JPanel {

    private static final Color COR_FUNDO = new Color(238, 238, 238);

    private final TurmaService turmaService = new TurmaService();
    private final TabelaTurmasModel tabelaModel = new TabelaTurmasModel();
    private final JTable tblTurmas = new JTable(tabelaModel);

    public PainelTurmas() {
        setLayout(new BorderLayout());
        setBackground(COR_FUNDO);

        add(criarTopo(), BorderLayout.NORTH);
        add(criarTabela(), BorderLayout.CENTER);

        carregarTabela();
    }

    private JPanel criarTopo() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        barra.setBackground(new Color(214, 214, 214));
        barra.add(criarBotao("Nova Turma", e -> novaTurma()));
        barra.add(criarBotao("Atualizar Lista", e -> carregarTabela()));
        barra.add(criarBotao("Editar Turma", e -> editarTurma()));
        barra.add(criarBotao("Remover Turma", e -> removerTurma()));

        JLabel titulo = new JLabel("Gestão de Turmas");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setBorder(BorderFactory.createEmptyBorder(20, 20, 12, 20));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(barra, BorderLayout.NORTH);
        topo.add(titulo, BorderLayout.CENTER);
        return topo;
    }

    private JPanel criarTabela() {
        tblTurmas.setRowHeight(30);
        tblTurmas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTurmas.setShowVerticalLines(false);
        tblTurmas.setFillsViewportHeight(true);
        tblTurmas.getTableHeader().setReorderingAllowed(false);
        tblTurmas.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        // Linhas alternadas (zebra) e espaçamento nas células
        tblTurmas.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean selecionada, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, selecionada, false, linha, coluna);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (!selecionada) {
                    setBackground(linha % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                }
                return this;
            }
        });

        int[] larguras = {60, 400, 200};
        for (int i = 0; i < larguras.length; i++) {
            tblTurmas.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }

        // Duplo clique numa linha abre a edição
        tblTurmas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblTurmas.getSelectedRow() >= 0) {
                    editarTurma();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblTurmas);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(170, 170, 170)));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JButton criarBotao(String texto, ActionListener acao) {
        JButton btn = new JButton(texto);
        btn.setFocusPainted(false);
        btn.addActionListener(acao);
        return btn;
    }

    // ---------- Ações ----------

    private void carregarTabela() {
        try {
            List<Turma> turmas = turmaService.listarTodas();
            tabelaModel.atualizarDados(turmas);
        } catch (RuntimeException e) {
            erro("Erro ao carregar as turmas:\n" + e.getMessage());
        }
    }

    private void novaTurma() {
        DialogTurma dialog = new DialogTurma(janelaPai(), null);
        dialog.setVisible(true);
        if (dialog.foiSalvo()) {
            carregarTabela();
        }
    }

    private void editarTurma() {
        Turma turma = turmaSelecionada();
        if (turma == null) {
            return;
        }
        DialogTurma dialog = new DialogTurma(janelaPai(), turma);
        dialog.setVisible(true);
        if (dialog.foiSalvo()) {
            carregarTabela();
        }
    }

    private void removerTurma() {
        Turma turma = turmaSelecionada();
        if (turma == null) {
            return;
        }
        int opcao = JOptionPane.showConfirmDialog(this,
                "Remover a turma \"" + turma.getNome() + "\"?",
                "Confirmar remoção", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcao != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            turmaService.remover(turma.getId());
            carregarTabela();
        } catch (RuntimeException e) {
            erro("Não foi possível remover a turma.\n"
               + "Ela pode ter alunos associados.\n\n" + e.getMessage());
        }
    }

    // ---------- Auxiliares ----------

    private Turma turmaSelecionada() {
        int linha = tblTurmas.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma turma na tabela.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return tabelaModel.getTurmaNaLinha(linha);
    }

    private Frame janelaPai() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }

    private void erro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}